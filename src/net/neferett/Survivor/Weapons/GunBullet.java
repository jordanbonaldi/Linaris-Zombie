
package net.neferett.Survivor.Weapons;

public class GunBullet {
    private int m_charger;
    private int m_bullet;

    public GunBullet(int bullet, int charger) {
        this.m_bullet = bullet;
        this.m_charger = charger;
    }

    public GunBullet(GunBullet bullet) {
        this.m_bullet = bullet.getBullet();
        this.m_charger = bullet.getCharger();
    }

    public int getCharger() {
        return this.m_charger;
    }

    public int getBullet() {
        return this.m_bullet;
    }

    public void setBullet(int nb) {
        this.m_bullet = nb;
    }

    public void addBullet(int nb) {
        this.m_bullet += nb;
    }

    public void setCharger(int nb) {
        this.m_charger = nb;
    }

    public void decrementBullet(int nb) {
        this.m_bullet -= nb;
    }

    public void decrementCharger(int nb) {
        this.m_charger -= nb;
    }

    public boolean equals(Object other) {
        if (!(other instanceof GunBullet)) {
            return false;
        }
        GunBullet bullet = (GunBullet)other;
        if (this.m_bullet == bullet.getBullet() && this.m_charger == bullet.getCharger()) {
            return true;
        }
        return false;
    }
}

