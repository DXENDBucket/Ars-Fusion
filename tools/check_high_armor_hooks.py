"""Inspect real Ars bytecode for the native hooks used by the tier-four/five extension.

This runs javap only. It neither launches Minecraft nor proves that a Mixin applied at runtime.
"""
import argparse
import json
import re
import subprocess


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('jars',nargs='+')
    parser.add_argument('--javap',required=True)
    args=parser.parse_args()
    reports=[]
    for jar in args.jars:
        def inspect(name):
            return subprocess.check_output([args.javap,'-c','-p','-classpath',jar,name],text=True,encoding='utf-8',errors='replace')
        def method(text,name):
            match=re.search(r'^  (?:public|private|protected)[^\n]*\b'+name+r'\([^\n]*\n.*?(?=^  (?:public|private|protected)|\Z)',text,re.M|re.S)
            if not match:raise AssertionError(f'{jar}: method {name} missing')
            return match.group()
        renderer=inspect('com.hollingsworth.arsnouveau.client.renderer.tile.AlterationTableRenderer')
        perks=method(renderer,'renderPerks');slate=method(renderer,'renderSlate')
        checks={
            'render_perks_three_constant_once':len(re.findall(r'\biconst_3\b',perks))==1,
            'render_perks_two_bone_calls':perks.count('GeoModel.getBone:')==2,
            'perks_spacing_present':'double 0.175d' in perks,
            'perks_scale_present':'float 0.18f' in perks,
        }
        if 'BakedGeoModel' in slate.splitlines()[0]:
            checks['forge_slate_model_variant_path']='setSlateRow:' in slate and 'java/util/List.subList:' in slate
            checks['forge_perks_signature']='float, net.minecraft.client.renderer.MultiBufferSource, com.mojang.blaze3d.vertex.VertexConsumer, int, int, float)' in perks.splitlines()[0]
        else:
            checks.update({
                'render_slate_three_constants_twice':len(re.findall(r'\biconst_3\b',slate))==2,
                'render_slate_three_bone_calls':slate.count('GeoModel.getBone:')==3,
                'slate_spacing_present':'double 0.175d' in slate,
                'slate_scale_present':'float -0.02f' in slate,
                'neo_perks_signature':'PoseStack, net.minecraft.client.renderer.MultiBufferSource, int, int)' in perks.splitlines()[0],
            })
        tile=inspect('com.hollingsworth.arsnouveau.common.block.tile.AlterationTile')
        checks['add_perk_three_constant_once']=len(re.findall(r'\biconst_3\b',method(tile,'addPerkStack')))==1
        jei=inspect('com.hollingsworth.arsnouveau.client.jei.ArmorUpgradeRecipeCategory')
        checks['jei_native_provider_list_call']=jei.count('PerkRegistry.getPerkProviderItems:()Ljava/util/List;')==1
        assert all(checks.values()),(jar,checks)
        reports.append({'jar':jar,'checks':checks,'runtime_mixin_application':'not tested'})
    print(json.dumps(reports,indent=2))


if __name__=='__main__':main()
