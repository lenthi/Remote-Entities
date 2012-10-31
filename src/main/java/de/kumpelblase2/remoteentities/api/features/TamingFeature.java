package de.kumpelblase2.remoteentities.api.features;

import org.bukkit.entity.Player;

public interface TamingFeature extends Feature
{
	public boolean isTamed();
	public void untame();
	public void tame(Player inPlayer);
	public Player getTamer();
}