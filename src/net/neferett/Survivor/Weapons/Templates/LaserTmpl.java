
package net.neferett.Survivor.Weapons.Templates;

import net.neferett.Survivor.Weapons.Templates.GunWeaponTmpl;
import org.bukkit.Material;

public class LaserTmpl
extends GunWeaponTmpl {
    public LaserTmpl(String name, Material material) {
        super(name, material);
        this.m_isMain = false;
    }
}

