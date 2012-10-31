package de.kumpelblase2.remoteentities.api.thinking.goals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.MathHelper;
import net.minecraft.server.PathEntity;
import net.minecraft.server.Vec3D;
import net.minecraft.server.Village;
import net.minecraft.server.VillageDoor;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.thinking.DesireBase;
import de.kumpelblase2.remoteentities.nms.RandomPositionGenerator;

public class DesireMoveThroughVillage extends DesireBase
{
	protected boolean m_onlyNight;
	protected PathEntity m_path;
	protected VillageDoor m_nextDoor;
	protected List<VillageDoor> m_doors = new ArrayList<VillageDoor>();
	
	public DesireMoveThroughVillage(RemoteEntity inEntity, boolean inOnlyNight)
	{
		super(inEntity);
		this.m_onlyNight = inOnlyNight;
		this.m_type = 1;
	}

	@Override
	public boolean shouldExecute()
	{
		this.cleanupDoors();
		EntityLiving entity = this.getRemoteEntity().getHandle();
		if(this.m_onlyNight && entity.world.s())
			return false;
		else
		{
			Village nearestVillage = entity.world.villages.getClosestVillage(MathHelper.floor(entity.locX), MathHelper.floor(entity.locY), MathHelper.floor(entity.locZ), 0);
			
			if(nearestVillage == null)
				return false;
			else
			{
				this.m_nextDoor = this.getNearestDoor(nearestVillage);
				if(this.m_nextDoor == null)
					return false;
				else
				{
					boolean flag = entity.getNavigation().c();
					entity.getNavigation().b(false);
					this.m_path = entity.getNavigation().a(this.m_nextDoor.locX, this.m_nextDoor.locY, this.m_nextDoor.locZ);
					entity.getNavigation().b(flag);
					if(this.m_path != null)
						return true;
					else
					{
						Vec3D vec = RandomPositionGenerator.a(entity, 10, 7, Vec3D.a().create(this.m_nextDoor.locX, this.m_nextDoor.locY, this.m_nextDoor.locZ));
						
						if(vec == null)
							return false;
						else
						{
							entity.getNavigation().b(false);
							this.m_path = entity.getNavigation().a(vec.a, vec.b, vec.c);
							entity.getNavigation().b(flag);
							return this.m_path != null;
						}
					}
				}
			}
		}
	}
	
	@Override
	public boolean canContinue()
	{
		if(this.getRemoteEntity().getHandle().getNavigation().f())
			return false;
		else
		{
			float f = this.getRemoteEntity().getHandle().width + 4;
			return this.getRemoteEntity().getHandle().e(this.m_nextDoor.locX, this.m_nextDoor.locY, this.m_nextDoor.locZ) > f * f;
		}
	}
	
	@Override
	public void startExecuting()
	{
		this.getRemoteEntity().getHandle().getNavigation().a(this.m_path, this.getRemoteEntity().getSpeed());
	}
	
	@Override
	public void stopExecuting()
	{
		if(this.getRemoteEntity().getHandle().getNavigation().f() || this.getRemoteEntity().getHandle().e(this.m_nextDoor.locX, this.m_nextDoor.locY, this.m_nextDoor.locZ) < 16)
			this.m_doors.add(this.m_nextDoor);
	}
	
	protected void cleanupDoors()
	{
		if(this.m_doors.size() > 15)
			this.m_doors.remove(0);
	}
	
	protected boolean isDoorInList(VillageDoor inDoor)
	{
		Iterator<VillageDoor> it = this.m_doors.iterator();
		VillageDoor door;
		
		do
		{
			if(!it.hasNext())
				return false;
			
			door = it.next();
		}
		while(door.locX != inDoor.locX || door.locY != inDoor.locY || door.locZ != inDoor.locZ);
		
		return true;
	}
	
	protected VillageDoor getNearestDoor(Village inVillage)
	{
		VillageDoor nearest = null;
		int dist = Integer.MAX_VALUE;
		@SuppressWarnings("unchecked")
		List<VillageDoor> allDoors = inVillage.getDoors();
		Iterator<VillageDoor> it = allDoors.iterator();
		EntityLiving entity = this.getRemoteEntity().getHandle();
		
		while(it.hasNext())
		{
			VillageDoor door = it.next();
			int ddist = door.b(MathHelper.floor(entity.locX), MathHelper.floor(entity.locY), MathHelper.floor(entity.locZ));
			
			if(ddist < dist && !this.isDoorInList(door))
			{
				nearest = door;
				dist = ddist;
			}
		}
		
		return nearest;
	}
}
