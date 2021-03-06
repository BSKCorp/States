package net.fexcraft.mod.states.api.capabilities;

import com.google.common.collect.ImmutableMap;

import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public interface WorldCapability {

	public void setWorld(World world);

	public int getNewMunicipalityId() throws Exception;

	public NBTBase writeToNBT(Capability<WorldCapability> capability, EnumFacing side);

	public void readFromNBT(Capability<WorldCapability> capability, EnumFacing side, NBTBase nbt);

	public int getNewDistrictId() throws Exception;

	public int getNewStateId() throws Exception;
	
	public ImmutableMap<Integer, District> getDistricts();
	
	public ImmutableMap<Integer, Municipality> getMunicipalities();
	
	public ImmutableMap<Integer, State> getStates();

}
