package net.fexcraft.mod.states.cmds;

import net.fexcraft.mod.lib.api.common.fCommand;
import net.fexcraft.mod.lib.util.common.Print;
import net.fexcraft.mod.lib.util.json.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class DebugCmd extends CommandBase {

	@Override
	public String getName(){
		return "st-debug";
	}

	@Override
	public String getUsage(ICommandSender sender){
		return "/st-debug";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length == 0){
			Print.chat(sender, "&7/st-debug chunk");
			Print.chat(sender, "&7/st-debug chunks");
			Print.chat(sender, "&7/st-debug district");
			Print.chat(sender, "&7/st-debug districts");
			return;
		}
		if(sender instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame.");
			return;
		}
		Chunk chunk = StateUtil.getChunk((EntityPlayer)sender);
		switch(args[0]){
			case "chunks":{
				States.CHUNKS.cellSet().forEach((elm) -> {
					Print.log(elm.getValue().toJsonObject().toString());
				});
				Print.chat(sender, "&7Chunks loaded: &a" + States.CHUNKS.size());
				Print.chat(sender, "Chunk JSON's printed into console.");
				return;
			}
			case "ck": case "chunk":{
				Print.chat(sender, JsonUtil.setPrettyPrinting(chunk.toJsonObject()));
				return;
			}
			case "district":{
				Print.chat(sender, JsonUtil.setPrettyPrinting(chunk.getDistrict().toJsonObject()));
				return;
			}
			case "districts":{
				States.DISTRICTS.values().forEach((elm) -> {
					Print.log(elm.toJsonObject().toString());
				});
				Print.chat(sender, "&Districts loaded: &a" + States.CHUNKS.size());
				Print.chat(sender, "Districts JSON's printed into console.");
				return;
			}
		}
	}

}