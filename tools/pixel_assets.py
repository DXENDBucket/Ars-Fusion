"""Reproducible pixel-asset finishing and Minecraft resource export; no network/API calls.

Run `python tools/pixel_assets.py --help`. AIGC generation remains a separate imagegen
step. Original images and actual prompts are archived before any local processing.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import shutil
from pathlib import Path

from PIL import Image, ImageDraw, ImageOps, __version__ as PILLOW_VERSION

ROOT = Path(__file__).resolve().parents[1]
WORK = ROOT / 'art/pixel/work'
ALGORITHM = 'pixel-assets-1'
LAYOUTS = {
    'generated': ('sprite',), 'handheld': ('sprite',), 'cube_all': ('all',),
    'cube_column': ('side', 'end'), 'cube_bottom_top': ('side', 'top', 'bottom'), 'cross': ('cross',),
}
IDENTIFIER = re.compile(r'^[a-z0-9_]+$')


def digest(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def canonical(value) -> bytes:
    return json.dumps(value, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode('utf-8')


def write_json(path: Path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def read_spec(path: Path) -> dict:
    spec = json.loads(path.read_text(encoding='utf-8'))
    if spec.get('palette_file'):
        if spec.get('palette'):
            raise ValueError('Choose palette or palette_file, not both')
        palette_path = Path(spec['palette_file'])
        if not palette_path.is_absolute(): palette_path = ROOT / palette_path
        spec['palette'] = json.loads(palette_path.read_text(encoding='utf-8'))['colors']
    if spec.get('schema_version') != 1:
        raise ValueError('Unsupported schema_version; expected 1')
    for key in ('namespace', 'id'):
        if not isinstance(spec.get(key), str) or not IDENTIFIER.fullmatch(spec[key]):
            raise ValueError(f'{key} must contain only lowercase ASCII letters, digits and underscores')
    kind, layout = spec.get('kind'), spec.get('layout')
    if kind not in ('item', 'block') or layout not in LAYOUTS:
        raise ValueError('Unknown kind or layout')
    if (kind == 'item') != (layout in ('generated', 'handheld')):
        raise ValueError('Item/block layout mismatch')
    size = spec.get('size', 16)
    if type(size) is not int or size not in (16, 32) or (kind == 'block' and size != 16):
        raise ValueError('Use 16px blocks and 16px/32px items')
    if set(spec.get('textures', {})) != set(LAYOUTS[layout]):
        raise ValueError(f'{layout} requires exactly these texture roles: {LAYOUTS[layout]}')
    for role, face in spec['textures'].items():
        if not isinstance(face, dict) or not isinstance(face.get('subject'), str) or not face['subject'].strip():
            raise ValueError(f'{role} needs a subject')
    options = spec.get('processing', {})
    for key, default, low, high in [('colors', 16, 2, 32), ('alpha_threshold', 128, 1, 254), ('padding', 1, 0, size // 4)]:
        value = options.get(key, default)
        if type(value) is not int or not low <= value <= high:
            raise ValueError(f'Invalid processing.{key}: expected {low}..{high}')
    if options.get('sampling', 'box') not in ('box', 'nearest'):
        raise ValueError('sampling must be box or nearest')
    palette = spec.get('palette', [])
    if not isinstance(palette, list) or (palette and not 2 <= len(palette) <= 32):
        raise ValueError('palette must be empty (adaptive) or contain 2..32 colors')
    for color in palette:
        if not isinstance(color, str) or not re.fullmatch(r'#[0-9a-fA-F]{6}', color):
            raise ValueError(f'Invalid palette color: {color}')
    return spec


def asset_dir(spec: dict, work: Path = WORK) -> Path:
    return work / f"{spec['namespace']}.{spec['id']}"


def prompt(spec: dict, role: str) -> str:
    face = spec['textures'][role]
    size = spec.get('size', 16)
    sprite = spec['kind'] == 'item' or spec['layout'] == 'cross'
    lines = [
        'Use case: stylized-concept',
        f"Asset type: Minecraft {'inventory item sprite' if spec['kind'] == 'item' else 'block texture'}, role {role}",
        f"Primary request: {face['subject']}",
        f"Style/medium: vanilla-style pixel art, exactly {size} by {size} logical pixels, enlarged with hard square pixels.",
        'Use deliberate pixel clusters and a small material palette. Keep important forms readable at native size.',
    ]
    if spec.get('family'):
        lines.append(f"Material family: {spec['family']}. Preserve its characteristic hues and motifs across related assets.")
    for reference in spec.get('references', []) + face.get('references', []):
        lines.append(f"Input reference ({reference.get('role', 'style')}): {reference['label']}; local file {reference['path']}")
    lines.append('Reference images are style/silhouette guidance; only an explicitly labelled edit target should be preserved as an edit.')
    if sprite:
        lines += [
            'Scene/backdrop: genuinely transparent RGBA background, not a painted checkerboard or white background.',
            'Composition: one isolated subject, centered within a square canvas with one logical pixel of margin.',
            'Lighting: restrained upper-left highlights, readable darker edge pixels; no cast shadow outside the subject.',
            'Avoid: labels, frame, ground plane, extra objects, bloom, smooth gradients, antialiasing, fine noise.',
        ]
    else:
        lines += [
            'Scene/backdrop: fully opaque texture covering the entire square canvas.',
            'Composition: flat orthographic surface, no perspective cube, no margin, no frame or inset.',
            'Lighting: neutral material shading. No scene lighting, baked directional shadow, vignette or bloom.',
            'Make opposite edges join naturally when tiled. Avoid a distinctive centered object repeated on every block.',
            'Avoid: text, labels, transparency, a rendered 3D block, noisy tiny speckles and smooth gradients.',
        ]
    if spec.get('palette'):
        lines.append('Palette: ' + ', '.join(spec['palette']))
    if face.get('constraints'):
        lines.append('Additional constraints: ' + face['constraints'])
    lines += ['No logo, watermark or text.', 'Deliver one texture only; do not combine faces or variants into a contact sheet.']
    return '\n'.join(lines) + '\n'


def import_source(spec: dict, role: str, source: Path, prompt_file: Path | None, generator: str, work: Path = WORK,
                  parent_sha: str | None = None):
    if role not in spec['textures']:
        raise ValueError(f'Unknown texture role: {role}')
    if generator == 'builtin-imagegen' and prompt_file is None:
        raise ValueError('AIGC imports require --prompt-file containing the actual prompt used')
    with Image.open(source) as im:
        if getattr(im, 'n_frames', 1) != 1:
            raise ValueError('Animated sources need a separate animation workflow')
        im.verify()
    raw = source.read_bytes()
    sha = digest(raw)
    folder = asset_dir(spec, work) / 'sources' / role / sha
    folder.mkdir(parents=True, exist_ok=True)
    target = folder / ('original' + source.suffix.lower())
    if target.exists() and target.read_bytes() != raw:
        raise ValueError('Immutable source archive conflict')
    if not target.exists():
        shutil.copyfile(source, target)
    record = {'sha256': sha, 'file': target.name, 'generator': generator,
              'prompt_used': prompt_file.read_text(encoding='utf-8') if prompt_file else None}
    if parent_sha:
        if not re.fullmatch(r'[0-9a-f]{64}', parent_sha) or parent_sha == sha:
            raise ValueError('Invalid --parent-sha; use a different archived source hash')
        parent = asset_dir(spec, work) / 'sources' / role / parent_sha / 'source.json'
        if not parent.is_file(): raise ValueError('Parent source must already be imported for the same role')
        record['derived_from'] = parent_sha
    record_path = folder / 'source.json'
    if record_path.exists() and json.loads(record_path.read_text(encoding='utf-8')) != record:
        raise ValueError('This source already has different provenance; do not rewrite its prompt history')
    write_json(record_path, record)
    write_json(asset_dir(spec, work) / 'inputs' / f'{role}.json', {'source': str(record_path.relative_to(asset_dir(spec, work)))})
    return sha


def inputs(spec: dict, work: Path = WORK):
    result = {}
    folder = asset_dir(spec, work).resolve()
    for role in spec['textures']:
        pointer = json.loads((folder / 'inputs' / f'{role}.json').read_text(encoding='utf-8'))
        path = (folder / pointer['source']).resolve()
        if not path.is_relative_to(folder):
            raise ValueError('Source pointer leaves asset workspace')
        record = json.loads(path.read_text(encoding='utf-8'))
        source = (path.parent / record['file']).resolve()
        if not source.is_relative_to(folder) or digest(source.read_bytes()) != record['sha256']:
            raise ValueError(f'{role}: source archive changed or is invalid')
        result[role] = (source, record)
    return result


def revision(spec: dict, loaded: dict) -> str:
    return digest(canonical({'spec': spec, 'sources': {k: v[1] for k, v in loaded.items()},
                             'algorithm': ALGORITHM, 'pillow': PILLOW_VERSION}))[:16]


def quantize_visible(image: Image.Image, colors: int, palette: list[str]) -> Image.Image:
    pixels = list(image.getdata())
    visible = [p[:3] for p in pixels if p[3]]
    if not visible:
        raise ValueError('Processing removed every visible pixel')
    if palette:
        choices = [tuple(bytes.fromhex(c[1:])) for c in palette]
        mapping = {rgb: min(choices, key=lambda c: sum((c[i] - rgb[i]) ** 2 for i in range(3))) for rgb in set(visible)}
    else:
        strip = Image.new('RGB', (len(visible), 1)); strip.putdata(visible)
        reduced = strip.quantize(colors=colors, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE).convert('RGB')
        mapping = dict(zip(visible, reduced.getdata()))
    result = Image.new('RGBA', image.size)
    result.putdata([(*mapping[p[:3]], 255) if p[3] else (0, 0, 0, 0) for p in pixels])
    return result


def process(image: Image.Image, spec: dict) -> Image.Image:
    image = ImageOps.exif_transpose(image).convert('RGBA')
    options = spec.get('processing', {}); size = spec.get('size', 16)
    sprite = spec['kind'] == 'item' or spec['layout'] == 'cross'
    threshold = options.get('alpha_threshold', 128)
    method = Image.Resampling.NEAREST if options.get('sampling', 'box') == 'nearest' else Image.Resampling.BOX
    if sprite:
        if image.getchannel('A').getextrema()[0] == 255:
            raise ValueError('Sprite source is opaque: regenerate with genuine transparency; no automatic background removal')
        mask = image.getchannel('A').point(lambda a: 255 if a >= threshold else 0)
        bounds = mask.getbbox()
        if bounds is None:
            raise ValueError('Empty sprite or alpha threshold too high')
        if image.size != (size, size):
            cropped = image.crop(bounds)
            available = size - 2 * options.get('padding', 1)
            ratio = available / max(cropped.size)
            dims = (max(1, round(cropped.width * ratio)), max(1, round(cropped.height * ratio)))
            scaled = cropped.convert('RGBa').resize(dims, method).convert('RGBA')
            image = Image.new('RGBA', (size, size))
            image.paste(scaled, ((size - dims[0]) // 2, (size - dims[1]) // 2))
    else:
        if image.width != image.height:
            raise ValueError('Block face must be square; never silently stretch a rectangular texture')
        if image.getchannel('A').getextrema()[0] != 255:
            raise ValueError('Cube faces must be fully opaque')
        image = image.resize((size, size), method)
    image.putdata([p if p[3] >= threshold else (0, 0, 0, 0) for p in image.getdata()])
    return quantize_visible(image, options.get('colors', 16), spec.get('palette', []))


def rgb_distance(a, b):
    return sum(abs(a[i] - b[i]) for i in range(3)) / 3


def seam_metrics(image: Image.Image) -> dict:
    w, h = image.size
    x = sum(rgb_distance(image.getpixel((0, y)), image.getpixel((w - 1, y))) for y in range(h)) / h
    z = sum(rgb_distance(image.getpixel((i, 0)), image.getpixel((i, h - 1))) for i in range(w)) / w
    interior = [rgb_distance(image.getpixel((i, j)), image.getpixel((i + 1, j))) for j in range(h) for i in range(w - 1)]
    interior += [rgb_distance(image.getpixel((i, j)), image.getpixel((i, j + 1))) for j in range(h - 1) for i in range(w)]
    baseline = sum(interior) / len(interior)
    return {'horizontal': round(x, 2), 'vertical': round(z, 2), 'interior': round(baseline, 2),
            'ratio': round(max(x, z) / max(1, baseline), 2)}


def analyze(image: Image.Image, spec: dict) -> dict:
    pixels = list(image.getdata()); visible = [p for p in pixels if p[3]]
    errors, warnings = [], []
    if image.size != (spec.get('size', 16),) * 2: errors.append('wrong dimensions')
    if any(p[3] not in (0, 255) for p in pixels): errors.append('partial alpha remains')
    if not visible: errors.append('empty texture')
    sprite = spec['kind'] == 'item' or spec['layout'] == 'cross'
    if not sprite and len(visible) != len(pixels): errors.append('cube face contains transparency')
    color_count = len(set(p[:3] for p in visible))
    color_limit = len(spec['palette']) if spec.get('palette') else spec.get('processing', {}).get('colors', 16)
    if color_count > color_limit: errors.append('palette budget exceeded')
    report = {'size': list(image.size), 'colors': color_count,
              'palette': sorted('#' + bytes(p[:3]).hex() for p in set(visible)), 'visible_pixels': len(visible),
              'errors': errors, 'warnings': warnings}
    if sprite:
        edges = [(x, y) for x in range(image.width) for y in range(image.height)
                 if x in (0, image.width - 1) or y in (0, image.height - 1)]
        if any(image.getpixel(p)[3] for p in edges): warnings.append('subject touches canvas border')
        # Tiny detached islands may be intentional droplets, but should be reviewed at native size.
        remaining = {(x, y) for x in range(image.width) for y in range(image.height) if image.getpixel((x, y))[3]}
        islands = []
        while remaining:
            stack = [remaining.pop()]; count = 0
            while stack:
                x, y = stack.pop(); count += 1
                for dx in (-1, 0, 1):
                    for dy in (-1, 0, 1):
                        p = (x + dx, y + dy)
                        if p in remaining: remaining.remove(p); stack.append(p)
            islands.append(count)
        report['components'] = sorted(islands, reverse=True)
        if len(islands) > 1 and any(n <= 2 for n in islands): warnings.append('tiny detached pixel islands')
    else:
        report['seams'] = seam_metrics(image)
        if report['seams']['ratio'] > 1.8 and max(report['seams']['horizontal'], report['seams']['vertical']) > 24:
            warnings.append('opposite-edge contrast may expose tiling seams')
    return report


def preview(image: Image.Image, spec: dict, target: Path):
    size = image.width; scale = 12 if size == 16 else 8; large = size * scale
    sprite = spec['kind'] == 'item' or spec['layout'] == 'cross'
    if sprite:
        canvas = Image.new('RGB', (large * 3 + 32, large + 72), '#25252c'); draw = ImageDraw.Draw(canvas)
        for index, background in enumerate(('#16161c', '#e4dfd3', None)):
            x0 = index * large + 16
            draw.rectangle((x0, 28, x0 + large - 1, 28 + large - 1), fill=background or '#585864')
            if background is None:
                for y in range(size):
                    for x in range(size):
                        draw.rectangle((x0 + x * scale, 28 + y * scale, x0 + (x + 1) * scale - 1, 28 + (y + 1) * scale - 1),
                                       fill='#585864' if (x + y) % 2 else '#747480')
            enlarged = image.resize((large, large), Image.Resampling.NEAREST)
            canvas.paste(enlarged, (x0, 28), enlarged)
            canvas.paste(image, (x0, large + 36), image)
        draw.text((16, 8), 'Dark / light / alpha   |   native pixels below', fill='white')
    else:
        canvas = Image.new('RGB', (large * 3 + 32, large * 3 + large + 72), '#25252c'); draw = ImageDraw.Draw(canvas)
        enlarged = image.convert('RGB').resize((large, large), Image.Resampling.NEAREST)
        for y in range(3):
            for x in range(3): canvas.paste(enlarged, (16 + x * large, 28 + y * large))
        offset = Image.frombytes('RGBA', image.size, image.tobytes())
        from PIL import ImageChops
        offset = ImageChops.offset(offset, size // 2, size // 2)
        canvas.paste(offset.resize((large, large), Image.Resampling.NEAREST), (16, 36 + 3 * large))
        canvas.paste(image, (large + 32, 36 + 3 * large))
        draw.text((16, 8), '3x3 seamless tiling check', fill='white')
        draw.text((large + 32, 64 + 3 * large), 'Native / half-tile offset', fill='white')
    canvas.save(target)


def cube_preview(images: dict[str, Image.Image], layout: str, target: Path):
    """Approximate item/block view for review only; it is never exported as a texture."""
    from PIL import ImageEnhance
    top = images.get('top', images.get('end', images.get('all')))
    side = images.get('side', images.get('all'))
    if top is None or side is None: return
    canvas = Image.new('RGBA', (256, 288), '#25252c')
    faces = [
        (side, .85, [(16, 72), (128, 128), (128, 256), (16, 200)], (1 / 112, 0, -16 / 112, -1 / 256, 1 / 128, -.5)),
        (side, .65, [(128, 128), (240, 72), (240, 200), (128, 256)], (1 / 112, 0, -128 / 112, 1 / 256, 1 / 128, -1.5)),
        (top, 1., [(128, 16), (240, 72), (128, 128), (16, 72)], (1 / 224, 1 / 112, -5 / 7, -1 / 224, 1 / 112, 3 / 7)),
    ]
    for image, light, polygon, coefficients in faces:
        shaded = ImageEnhance.Brightness(image.convert('RGB')).enhance(light).convert('RGBA')
        warped = shaded.transform(canvas.size, Image.Transform.AFFINE, tuple(c * image.width for c in coefficients),
                                  resample=Image.Resampling.NEAREST)
        mask = Image.new('L', canvas.size); ImageDraw.Draw(mask).polygon(polygon, fill=255)
        canvas.paste(warped, (0, 0), mask)
    ImageDraw.Draw(canvas).text((16, 270), 'Approximate cube / not game lighting', fill='white')
    canvas.convert('RGB').save(target)


def prepare(spec: dict, work: Path = WORK) -> Path:
    loaded = inputs(spec, work); rev = revision(spec, loaded)
    folder = asset_dir(spec, work) / 'revisions' / rev
    folder.mkdir(parents=True, exist_ok=True)
    reports = {}; outputs = {}; images = {}
    for role, (source, record) in loaded.items():
        with Image.open(source) as original: image = process(original, spec)
        target = folder / f'{role}.png'; image.save(target)
        reports[role] = analyze(image, spec)
        images[role] = image
        outputs[role] = digest(target.read_bytes())
        preview(image, spec, folder / f'{role}-preview.png')
    if spec['kind'] == 'block' and spec['layout'] != 'cross':
        cube_preview(images, spec['layout'], folder / 'cube-preview.png')
    write_json(folder / 'report.json', {'textures': reports, 'note': 'Automated checks cannot certify silhouette, material readability or visual quality.'})
    write_json(folder / 'provenance.json', {'revision': rev, 'algorithm': ALGORITHM, 'pillow': PILLOW_VERSION,
                                          'spec': spec, 'sources': {k: v[1] for k, v in loaded.items()}, 'outputs': outputs})
    return folder


def export_plan(spec: dict, folder: Path, assets: Path):
    namespace, name, layout = spec['namespace'], spec['id'], spec['layout']
    base = assets / namespace; kind = spec['kind']
    files = {}; refs = {}
    for role in spec['textures']:
        texture_name = name if len(spec['textures']) == 1 else f'{name}_{role}'
        refs[role] = f'{namespace}:{kind}/{texture_name}'
        files[base / f'textures/{kind}/{texture_name}.png'] = (folder / f'{role}.png').read_bytes()
    if kind == 'item':
        files[base / f'models/item/{name}.json'] = canonical({'parent': 'minecraft:item/' + layout, 'textures': {'layer0': refs['sprite']}})
    else:
        files[base / f'models/block/{name}.json'] = canonical({'parent': 'minecraft:block/' + layout, 'textures': refs})
        item_model = {'parent': 'minecraft:item/generated', 'textures': {'layer0': refs['cross']}} if layout == 'cross' else {'parent': f'{namespace}:block/{name}'}
        files[base / f'models/item/{name}.json'] = canonical(item_model)
        if layout == 'cube_column':
            variants = {'axis=y': {'model': f'{namespace}:block/{name}'},
                        'axis=z': {'model': f'{namespace}:block/{name}', 'x': 90},
                        'axis=x': {'model': f'{namespace}:block/{name}', 'x': 90, 'y': 90}}
        else:
            variants = {'': {'model': f'{namespace}:block/{name}'}}
        files[base / f'blockstates/{name}.json'] = canonical({'variants': variants})
    return files


def export(spec: dict, assets: Path, replace: bool = False, dry_run: bool = False, work: Path = WORK,
           records: Path = ROOT / 'art/pixel/records'):
    loaded = inputs(spec, work)
    folder = asset_dir(spec, work) / 'revisions' / revision(spec, loaded)
    provenance = json.loads((folder / 'provenance.json').read_text(encoding='utf-8'))
    for role in spec['textures']:
        texture = folder / f'{role}.png'
        if digest(texture.read_bytes()) != provenance['outputs'][role]:
            raise ValueError(f'{role}: staged texture changed; regenerate after changing the spec/source')
        with Image.open(texture) as image:
            report = analyze(image.convert('RGBA'), spec)
        if report['errors']:
            raise ValueError(f"{role}: {report['errors']}")
    files = export_plan(spec, folder, assets)
    files[records / f"{spec['namespace']}.{spec['id']}.json"] = canonical(provenance)
    # Preflight the whole set; an existing late model must not leave earlier textures installed.
    conflicts = [str(p) for p in files if p.exists()]
    if conflicts and not replace:
        raise ValueError('Existing resources; use another id or explicit --replace:\n' + '\n'.join(conflicts))
    if not dry_run:
        for path, data in files.items():
            path.parent.mkdir(parents=True, exist_ok=True)
            if path.suffix == '.json':
                data = (json.dumps(json.loads(data), ensure_ascii=False, indent=2) + '\n').encode('utf-8')
            path.write_bytes(data)
    return files


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest='command', required=True)
    for command in ('prompt', 'import', 'prepare', 'export'):
        child = commands.add_parser(command)
        child.add_argument('spec', type=Path)
        if command == 'prompt': child.add_argument('--output', type=Path)
        if command == 'import':
            child.add_argument('role'); child.add_argument('source', type=Path)
            child.add_argument('--prompt-file', type=Path)
            child.add_argument('--parent-sha', help='Archived parent image hash for AIGC edits or manual pixel cleanup')
            child.add_argument('--generator', choices=('builtin-imagegen', 'manual', 'procedural'), default='builtin-imagegen')
        if command == 'export':
            child.add_argument('--assets', type=Path, default=ROOT / 'src/main/resources/assets')
            child.add_argument('--replace', action='store_true'); child.add_argument('--dry-run', action='store_true')
    args = parser.parse_args()
    try:
        spec = read_spec(args.spec)
        if args.command == 'prompt':
            folder = args.output or asset_dir(spec) / 'prompts'
            folder.mkdir(parents=True, exist_ok=True)
            for role in spec['textures']:
                path = folder / f'{role}.txt'; path.write_text(prompt(spec, role), encoding='utf-8'); print(path)
        elif args.command == 'import': print(import_source(spec, args.role, args.source, args.prompt_file, args.generator, parent_sha=args.parent_sha))
        elif args.command == 'prepare': print(prepare(spec))
        else:
            for path in export(spec, args.assets, args.replace, args.dry_run): print(path)
    except (ValueError, KeyError, OSError, json.JSONDecodeError) as error:
        parser.exit(1, f'pixel-assets: {error}\n')


if __name__ == '__main__':
    main()
