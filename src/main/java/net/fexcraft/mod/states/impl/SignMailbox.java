package net.fexcraft.mod.states.impl;

import java.util.UUID;

import net.fexcraft.mod.lib.capabilities.sign.SignCapability;
import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.events.PlayerEvents;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.StatesPermissions;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.CapabilityItemHandler;

public class SignMailbox implements SignCapability.Listener {
	
	private static final ResourceLocation RESLOC = new ResourceLocation("states:mailbox");
	private boolean active;
	private UUID recipient;
	private String type;

	@Override
	public ResourceLocation getId(){
		return RESLOC;
	}

	@Override
	public boolean isActive(){
		return active;
	}

	@Override
	public boolean onPlayerInteract(SignCapability cap, PlayerInteractEvent event, IBlockState state, TileEntitySign tileentity){
		if(event.getWorld().isRemote){ return false; }
		if(!active){
			if(tileentity.signText[0].getUnformattedText().toLowerCase().equals("[st-mailbox]")){
				TileEntity te = event.getWorld().getTileEntity(getPosAtBack(state, tileentity));
				if(te == null){ Print.chat(event.getEntityPlayer(), "Not a valid mailbox position."); return false; }
				EnumFacing facing = state.getBlock() instanceof BlockWallSign ? EnumFacing.getFront(tileentity.getBlockMetadata()) : null;
				if(!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)){
					Print.chat(event.getEntityPlayer(), "Block/TileEntity cannot store items."); return false;
				}
				if(!PlayerEvents.checkAccess(te.getWorld(), te.getPos(), te.getWorld().getBlockState(te.getPos()), event.getEntityPlayer())){
					Print.chat(event.getEntityPlayer(), "Block/TileEntity cannot be accessed."); return false;
				}
				Chunk chunk = StateUtil.getChunk(tileentity.getPos());
				String type = tileentity.signText[1].getUnformattedText().toLowerCase();
				switch(type){
					case "state":{
						if(!StatesPermissions.hasPermission(event.getEntityPlayer(), "state.set.mailbox", chunk.getState())){
							Print.chat(event.getEntityPlayer(), "No permission to set the State Mailbox."); return false;
						}
					}
					case "municipality":{
						if(!StatesPermissions.hasPermission(event.getEntityPlayer(), "municipality.set.mailbox", chunk.getMunicipality())){
							Print.chat(event.getEntityPlayer(), "No permission to set the Municipality Mailbox."); return false;
						}
					}
					case "district":{
						if(!StatesPermissions.hasPermission(event.getEntityPlayer(), "district.set.mailbox", chunk.getDistrict())){
							Print.chat(event.getEntityPlayer(), "No permission to set the District Mailbox."); return false;
						}
					}
					case "company": break;//TODO
					case "player":{
						String rec = tileentity.signText[2].getUnformattedText().toLowerCase();
						com.mojang.authlib.GameProfile prof = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(rec);
						if(prof == null){
							Print.chat(event.getEntityPlayer(), "Couldn't find player UUID in cache.");
							return false;
						}
						if(prof.getId().equals(event.getEntityPlayer().getGameProfile().getId()) || StatesPermissions.hasPermission(event.getEntityPlayer(), "admin", null)){
							this.recipient = prof.getId();
							tileentity.signText[1] = Formatter.newTextComponentString(prof.getName());
							tileentity.signText[2] = Formatter.newTextComponentString("");
						}//TODO municipality check
						else{
							Print.chat(event.getEntityPlayer(), "No permission to set mailbox of that player.");
						}
						break;
					}
					default:{
						Print.chat(event.getEntityPlayer(), "Invalid mailbox type.");
						return false;
					}
				}
				tileentity.signText[0] = Formatter.newTextComponentString("&0[&3Mailbox&0]");
				try{
					switch(type){
						case "state": chunk.getState().setMailbox(tileentity.getPos()); break;
						case "municipality": chunk.getMunicipality().setMailbox(tileentity.getPos()); break;
						case "district": chunk.getDistrict().setMailbox(tileentity.getPos()); break;
						case "companry": break;//TODO
						case "player":{
							if(event.getEntityPlayer().getGameProfile().getId().equals(recipient)){
								event.getEntityPlayer().getCapability(StatesCapabilities.PLAYER, null).setMailbox(tileentity.getPos());
							}
							else{
								StateUtil.getPlayer(recipient, true).setMailbox(tileentity.getPos());
							}
							break;
						}
					}
					this.type = type; cap.setActive(); this.active = true;
					this.sendUpdate(tileentity);
				}
				catch(Exception e){
					e.printStackTrace();
					Print.chat(event.getEntityPlayer(), "Error occured, check log for info.");
				}
				return true;
			}
			else return false;
		}
		else{
			Print.chat(event.getEntityPlayer(), "&k!000-000!000-000!");
		}
		return false;
	}

	@Override
	public NBTBase writeToNBT(Capability<SignCapability> capability, EnumFacing side){
		if(!active){ return null; }
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("sign:active", active);
		compound.setString("sign:type", type);
		if(recipient != null) compound.setString("sign:recipient", recipient.toString());
		return compound;
	}

	@Override
	public void readNBT(Capability<SignCapability> capability, EnumFacing side, NBTBase nbt){
		if(nbt == null || !(nbt instanceof NBTTagCompound)){ active = false; return; }
		NBTTagCompound compound = (NBTTagCompound)nbt;
		try{
			active = compound.getBoolean("sign:active");
			type = compound.getString("sign:type");
			recipient = compound.hasKey("sign:recipient") ? UUID.fromString(compound.getString("sign:recipient")) : null;
		}
		catch(Exception e){
			e.printStackTrace();
			active = false;
		}
	}
	
}