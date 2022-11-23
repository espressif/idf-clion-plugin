package com.espressif.idf.configurations.debugger.util;

import com.intellij.openapi.diagnostic.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EspConfigParser
{
	private final String espConfigPath;
	private final static Logger LOG = Logger.getInstance(EspConfigParser.class);

	public EspConfigParser(String espConfigPath)
	{
		this.espConfigPath = espConfigPath;
	}

	public List<String> getEspTargets()
	{
		List<String> targets = new ArrayList<>();
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(espConfigPath))
		{
			JSONObject json = (JSONObject) jsonParser.parse(reader);
			JSONArray targetsArray = (JSONArray) json.get("targets");
			targetsArray.forEach(target -> targets.add((String) ((JSONObject) target).get("id")));
			reader.close();
		}
		catch (Exception e)
		{
			LOG.error(e);
		}
		return targets;
	}

	public List<String> getEspFlashVoltages()
	{
		List<String> voltages = new ArrayList<String>();
		JSONObject voltageOption;
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(espConfigPath))
		{
			JSONObject json = (JSONObject) jsonParser.parse(reader);
			JSONArray optionsArray = (JSONArray) json.get("options");
			voltageOption = (JSONObject) optionsArray.stream()
					.filter(option -> ((JSONObject) option).get("name").equals("ESP_FLASH_VOLTAGE")).findFirst()
					.orElse(null);
			((JSONArray) voltageOption.get("values")).forEach(value -> voltages.add(value.toString()));
			reader.close();
		}
		catch (Exception e)
		{
			LOG.error(e);
		}

		return voltages;

	}

	public Map<String, JSONArray> getBoardsConfigs(String target)
	{
		Map<String, JSONArray> boardsConfigs = new HashMap<>();
		List<JSONObject> objects = new ArrayList<>();
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(espConfigPath))
		{
			JSONObject json = (JSONObject) jsonParser.parse(reader);
			JSONArray boardsArray = (JSONArray) json.get("boards"); // $NON-NLS-1$
			boardsArray.forEach(board -> objects.add((JSONObject) board));
			for (JSONObject object : objects)
			{
				if (object.get("target").equals(target)) // $NON-NLS-1$
				{
					boardsConfigs.put((String) object.get("name"), // $NON-NLS-1$
							((JSONArray) object.get("config_files"))); // $NON-NLS-1$
				}
			}
			reader.close();
		}
		catch (Exception e)
		{
			LOG.error(e);
		}

		return boardsConfigs;
	}

	public boolean hasBoardConfigJson()
	{
		return new File(espConfigPath).exists();
	}
}
