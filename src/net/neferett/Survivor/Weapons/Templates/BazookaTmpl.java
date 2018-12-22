
package net.neferett.Survivor.Weapons.Templates;

import net.neferett.Survivor.Weapons.Templates.GunWeaponTmpl;
import org.bukkit.Material;

public class BazookaTmpl
extends GunWeaponTmpl {
    public BazookaTmpl(String name, Material material) {
        super(name, material);
        this.m_isMain = false;
    }
}

