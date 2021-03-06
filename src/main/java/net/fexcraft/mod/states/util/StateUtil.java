package net.fexcraft.mod.states.util;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;

import net.fexcraft.mod.lib.util.common.Formatter;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.common.Static;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.api.District;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.api.root.AnnounceLevel;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.fexcraft.mod.states.impl.GenericDistrict;
import net.fexcraft.mod.states.impl.GenericMunicipality;
import net.fexcraft.mod.states.impl.GenericPlayer;
import net.fexcraft.mod.states.impl.GenericState;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;

public class StateUtil extends TimerTask {
    
    public static @Nullable Chunk getChunk(int x, int z){
        return States.CHUNKS.get(new ChunkPos(x, z));
    }
    
    public static @Nullable Chunk getChunk(EntityPlayer player){
        return getChunk(player.getPosition());
    }
    
    public static @Nullable Chunk getChunk(BlockPos pos){
        return getChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }
    
    public static @Nullable Chunk getChunk(ChunkPos pos){
        return States.CHUNKS.get(pos);
    }
    
    public static @Nullable Chunk getChunk(net.minecraft.world.chunk.Chunk chunk){
        if(chunk != null){
            return chunk.getCapability(StatesCapabilities.CHUNK, null).getStatesChunk();
        }
        return null;
    }
    
    public static Chunk getTempChunk(int x, int z){
        Chunk chunk = getChunk(x, z);
        return chunk == null ? new GenericChunk(new ChunkPos(x, z), false) : chunk;
    }
    
    public static Chunk getTempChunk(ChunkPos pos){
        Chunk chunk = getChunk(pos);
        return chunk == null ? new GenericChunk(pos, false) : chunk;
    }
    
    public static Chunk getTempChunk(ResourceLocation ckpos){
        int x = Integer.parseInt(ckpos.getResourceDomain());
        int z = Integer.parseInt(ckpos.getResourcePath());
        return getTempChunk(x, z);
    }
	
	public static District getDistrict(int value){
		return getDistrict(value, true);
	}

	public static District getDistrict(int value, boolean wilderness){
		if(States.DISTRICTS.containsKey(value)){
			return States.DISTRICTS.get(value);
		}
		else if(District.getDistrictFile(value).exists() || isDefaultAvailable("districts", value)){
			District district = new GenericDistrict(value);
			States.DISTRICTS.put(value, district);
			return district;
		}
		else return wilderness ? States.DISTRICTS.get(-1) : null;
	}

	public static Municipality getMunicipality(int value){
		return getMunicipality(value, true);
	}

	public static Municipality getMunicipality(int value, boolean bool){
		if(States.MUNICIPALITIES.containsKey(value)){
			return States.MUNICIPALITIES.get(value);
		}
		else if(Municipality.getMunicipalityFile(value).exists() || isDefaultAvailable("municipalities", value)){
			Municipality municipality = new GenericMunicipality(value);
			States.MUNICIPALITIES.put(value, municipality);
			return municipality;
		}
		else return bool ? States.MUNICIPALITIES.get(-1) : null;
	}
	
	public static State getState(int value){
		return getState(value, true);
	}

	public static State getState(int value, boolean bool){
		if(States.STATES.containsKey(value)){
			return States.STATES.get(value);
		}
		else if(State.getStateFile(value).exists() || isDefaultAvailable("states", value)){
			State state = new GenericState(value);
			States.STATES.put(value, state);
			return state;
		}
		else return bool ? States.STATES.get(-1) : null;
	}

	public static boolean isUUID(String owner){
		try{
			UUID uuid = UUID.fromString(owner);
			return uuid != null;
		}
		catch(Exception e){
			return false;
		}
	}
	
	@Nullable
	public static PlayerCapability getPlayer(UUID uuid, boolean loadtemp){
		if(States.PLAYERS.containsKey(uuid)) return States.PLAYERS.get(uuid);
		else{
			if(loadtemp){
				PlayerCapability cap = GenericPlayer.getOfflineInstance(uuid);
				States.PLAYERS.put(uuid, cap); return cap;
			} else return null;
		}
	}
	
	@Nullable
	public static PlayerCapability getPlayer(String receiver, boolean loadtemp){
		try{
			UUID uuid = UUID.fromString(receiver);
			return getPlayer(uuid, loadtemp);
		}
		catch(Exception e){
			GameProfile gp = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(receiver);
			return gp == null ? null : getPlayer(gp.getId(), loadtemp);
		}
	}

	public static void announce(MinecraftServer server, String string){
		announce(server, AnnounceLevel.ALL, string, 0);
		return;
	}
	
	public static void announce(MinecraftServer server, AnnounceLevel level, String string, int range){
		announce(server, level, string, range, null);
	}

	public static void announce(MinecraftServer server, AnnounceLevel level, String string, int range, ICommandSender sender){
		server = server == null ? Static.getServer() : server;
		switch(level){
			case ALL:
				server.getPlayerList().sendMessage(new TextComponentString(Formatter.format(string)), true);
				MessageSender.toWebhook(null, string);
				break;
			case UNION:
				//TODO doesn't exists yet.
				break;
			case STATE:
				server.getPlayerList().getPlayers().forEach(player -> {
					PlayerCapability playerdata;
					if((playerdata = player.getCapability(StatesCapabilities.PLAYER, null)) != null && playerdata.getMunicipality().getState().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case STATE_ALL:
				server.getPlayerList().getPlayers().forEach(player -> {
					if(StateUtil.getChunk(player).getDistrict().getMunicipality().getState().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case MUNICIPALITY:
				server.getPlayerList().getPlayers().forEach(player -> {
					PlayerCapability playerdata;
					if((playerdata = player.getCapability(StatesCapabilities.PLAYER, null)) != null && playerdata.getMunicipality().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case MUNICIPALITY_ALL:
				server.getPlayerList().getPlayers().forEach(player -> {
					if(StateUtil.getChunk(player).getDistrict().getMunicipality().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case DISTRICT:
				server.getPlayerList().getPlayers().forEach(player -> {
					if(StateUtil.getChunk(player).getDistrict().getId() == range){
						Print.chat(player, string);
					}
				});
				break;
			case AREAL:
				List<EntityPlayerMP> players = getPlayersInRange(server, sender, range);
				players.forEach(player -> { Print.chat(player, string); });
				break;
			default:
				break;
		}
	}

	private static List<EntityPlayerMP> getPlayersInRange(MinecraftServer server, ICommandSender sender, int range){
		if(sender == null || server == null){ return new ArrayList<EntityPlayerMP>(); }
		List<EntityPlayerMP> list = new ArrayList<EntityPlayerMP>();
		Vec3d position = sender.getCommandSenderEntity().getPositionVector();
        for(EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()){
            if(player.dimension == sender.getCommandSenderEntity().dimension){
                double d4 = position.x - player.posX;
                double d5 = position.y - player.posY;
                double d6 = position.z - player.posZ;
                if(d4 * d4 + d5 * d5 + d6 * d6 < range * range){
                    list.add(player);
                }
            }
        }
		return list;
	}

    public static @Nullable net.minecraft.world.chunk.Chunk getChunk(Chunk chunk){
        return Static.getServer().worlds[0].getChunkProvider().getLoadedChunk(chunk.xCoord(), chunk.zCoord());
    }

	public static boolean isAdmin(EntityPlayer sender){
		return PermissionAPI.hasPermission(sender, States.ADMIN_PERM);
	}
	
	// --- /// --- //
	
	public static JsonObject getStateJson(int value){
		JsonElement elm = read(State.getStateFile(value));
		if(elm == null){
			InputStream in = StateUtil.class.getClassLoader().getResourceAsStream("assets/states/defaults/states/" + value + ".json");
			return in == null ? new JsonObject() : JsonUtil.getObjectFromInputStream(in);
		}
		else return elm.getAsJsonObject();
	}
	
	public static JsonObject getMunicipalityJson(int value){
		JsonElement elm = read(Municipality.getMunicipalityFile(value));
		if(elm == null){
			InputStream in = StateUtil.class.getClassLoader().getResourceAsStream("assets/states/defaults/municipalities/" + value + ".json");
			return in == null ? new JsonObject() : JsonUtil.getObjectFromInputStream(in);
		}
		else return elm.getAsJsonObject();
	}
	
	public static JsonObject getDistrictJson(int value){
		JsonElement elm = read(District.getDistrictFile(value));
		if(elm == null){
			InputStream in = StateUtil.class.getClassLoader().getResourceAsStream("assets/states/defaults/districts/" + value + ".json");
			return in == null ? new JsonObject() : JsonUtil.getObjectFromInputStream(in);
		}
		else return elm.getAsJsonObject();
	}
	
	private static boolean isDefaultAvailable(String type, int value){
		return StateUtil.class.getClassLoader().getResourceAsStream("assets/states/defaults/" + type + "/" + value + ".json") != null;
	}
	
	/** Copy from JsonUtil, adjusted to don't print too much into console. */
	public static final JsonElement read(File file, boolean bool){
		try{
			if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
			FileReader fr = new FileReader(file);
			JsonElement obj = JsonUtil.getParser().parse(fr); fr.close();
			return obj;
		}
		catch (Exception e) {
			if(bool){
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static final JsonElement read(File file){
		return read(file, false);
	}

	@Override
	public void run(){
		try{
			Print.debug("Scheduled check for inactive districts.");
			ImmutableList<District> collD = ImmutableList.copyOf(States.DISTRICTS.values());
			for(District dis : collD){
				if(States.CHUNKS.values().stream().filter(pre -> pre.getDistrict().getId() == dis.getId()).count() <= 0){
					States.DISTRICTS.remove(dis.getId());
					dis.save();
				}
			}
			Print.debug("Scheduled check for inactive municipalities.");
			ImmutableList<Municipality> collM = ImmutableList.copyOf(States.MUNICIPALITIES.values());
			for(Municipality mun : collM){
				if(States.DISTRICTS.values().stream().filter(pre -> pre.getMunicipality().getId() == mun.getId()).count() <= 0){
					States.MUNICIPALITIES.remove(mun.getId());
					mun.save(); mun.unload();
				}
			}
			Print.debug("Scheduled check for inactive states.");
			ImmutableList<State> collS = ImmutableList.copyOf(States.STATES.values());
			for(State state : collS){
				if(States.MUNICIPALITIES.values().stream().filter(pre -> pre.getState().getId() == state.getId()).count() <= 0){
					States.STATES.remove(state.getId());
					state.save(); state.unload();
				}
			}
			Print.debug("Scheduled check for offline user cache.");
			ImmutableList<PlayerCapability> collP = ImmutableList.copyOf(States.PLAYERS.values());
			for(PlayerCapability cap : collP){
				if(!cap.isOnlinePlayer()){
					States.PLAYERS.remove(cap.getUUID());
					cap.save(); cap.unload();
				}
			}
		}
		catch(Exception e){
			MessageSender.as(null, "SCHEDULED DATA UNLOAD ERRORED");
			e.printStackTrace();
		}
	}

	public static final void unloadAll(){
		for(District dis : States.DISTRICTS.values()){ dis.save(); }
		for(Municipality mun : States.MUNICIPALITIES.values()){ mun.save(); mun.unload(); }
		for(State state : States.STATES.values()){ state.save(); state.unload(); }
	}

	public static void clearAll(){
		States.DISTRICTS.clear(); States.MUNICIPALITIES.clear(); States.STATES.clear();
	}

}
