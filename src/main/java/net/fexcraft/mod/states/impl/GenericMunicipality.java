package net.fexcraft.mod.states.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.util.AccountManager;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.lib.util.lang.ArrayList;
import net.fexcraft.mod.lib.util.math.Time;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Municipality;
import net.fexcraft.mod.states.api.MunicipalityType;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.util.StateUtil;

public class GenericMunicipality implements Municipality {
	
	private int id;
	private String name;
	private long created, changed;
	private UUID creator, mayor;
	private Account account;
	private ArrayList<Integer> neighbors, districts;
	private ArrayList<UUID> citizen, council;
	private MunicipalityType type;
	private State state;
	
	public GenericMunicipality(int id){
		this.id = id;
		JsonObject obj = JsonUtil.get(this.getMunicipalityFile());
		name = JsonUtil.getIfExists(obj, "name", "Unnamed Place");
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		creator = UUID.fromString(obj.has("creator") ? obj.get("creator").getAsString() : States.CONSOLE_UUID);
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		account = AccountManager.INSTANCE.getAccount("municipality", id + "", true);
		mayor = obj.has("mayor") ? UUID.fromString(obj.get("mayor").getAsString()) : null;
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		districts = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "districts", new JsonArray()).getAsJsonArray());
		citizen = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "citizen", new JsonArray()).getAsJsonArray());
		council = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "council", new JsonArray()).getAsJsonArray());
		type = MunicipalityType.getType(this);
		state = StateUtil.getState(JsonUtil.getIfExists(obj, "state", -1).intValue());
	}

	@Override
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("name", name);
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		if(!(mayor == null)){ obj.addProperty("mayor", mayor.toString()); }
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		obj.add("districts", JsonUtil.getArrayFromIntegerList(districts));
		obj.add("citizen", JsonUtil.getArrayFromUUIDList(citizen));
		obj.add("council", JsonUtil.getArrayFromUUIDList(council));
		obj.addProperty("state", state.getId());
		return obj;
	}

	@Override
	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getMunicipalityFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
		//
		AccountManager.INSTANCE.saveAccount(account);
	}

	@Override
	public int getId(){
		return id;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public void setName(String new_name){
		name = new_name;
	}

	@Override
	public boolean isCapital(){
		return this.getState().getCapitalId() == this.getId();
	}

	@Override
	public void setChanged(long new_change){
		changed = new_change;
	}

	@Override
	public List<Integer> getNeighbors(){
		return neighbors;
	}

	@Override
	public List<Integer> getDistricts(){
		return districts;
	}

	@Override
	public long getCreated(){
		return created;
	}

	@Override
	public UUID getCreator(){
		return creator;
	}

	@Override
	public long getChanged(){
		return changed;
	}

	@Override
	public List<UUID> getCitizen(){
		return citizen;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	@Override
	public UUID getMayor(){
		return mayor;
	}

	@Override
	public void setMayor(UUID uuid){
		mayor = uuid;
	}

	@Override
	public List<UUID> getCouncil(){
		return council;
	}

	@Override
	public MunicipalityType getType(){
		return type;
	}
	
	/** Use this method when e.g. after updating the citizen list of a Municipality.*/
	@Override
	public void updateType(){
		type = MunicipalityType.getType(this);
	}

	@Override
	public State getState(){
		return state;
	}

	@Override
	public void setState(State new_state){
		state = new_state;
	}

}