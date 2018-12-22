
package net.neferett.Survivor.Weapons.Templates;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public abstract class WeaponTmpl {
    protected String m_name;
    protected Material m_material;
    protected int m_price = 0;

    public WeaponTmpl(String name, Material material) {
        this.m_name = name;
        this.m_material = material;
    }

    public String getName() {
        return ChatColor.translateAlternateColorCodes((char)'&', (String)this.m_name);
    }

    public Material getMaterial() {
        return this.m_material;
    }

    public WeaponTmpl setPrice(int price) {
        this.m_price = price;
        return this;
    }

    public int getPrice() {
        return this.m_price;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof WeaponTmpl)) {
            return false;
        }
        if (this == other) {
            return true;
        }
        WeaponTmpl weapon = (WeaponTmpl)other;
        if (weapon.getMaterial() == this.m_material) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        int result = 7;
        int multiplier = 17;
        result = 17 * result + this.m_material.hashCode();
        return result;
    }
}

