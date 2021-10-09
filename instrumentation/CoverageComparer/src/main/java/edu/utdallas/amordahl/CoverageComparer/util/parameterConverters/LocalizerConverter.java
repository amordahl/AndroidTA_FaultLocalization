package edu.utdallas.amordahl.CoverageComparer.util.parameterConverters;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

import edu.utdallas.amordahl.CoverageComparer.localizers.ILocalizer;
import edu.utdallas.amordahl.CoverageComparer.localizers.TarantulaLocalizer;

/**
 * 
 * @author austin
 *
 */
public class LocalizerConverter implements IStringConverter<ILocalizer<Path, String>> {
	// TODO Is there a better way to deal with generics here? I don't love that I've had to bind them here.

	public static final String[] SUPPORTED = new String[] {"tarantula"};
	private static Map<String, ILocalizer<Path, String>> nameToLocalizerMap;
	
	/**
	 * Sets up a map of strings to Localizers.
	 * @return
	 */
	private static Map<String, ILocalizer<Path, String>> getNameToLocalizerMap() {
		if (LocalizerConverter.nameToLocalizerMap != null) return nameToLocalizerMap;
		else {
			nameToLocalizerMap = new HashMap<String, ILocalizer<Path, String>>();
			nameToLocalizerMap.put("tarantula", new TarantulaLocalizer<Path, String>());
			return Collections.unmodifiableMap(nameToLocalizerMap);
		}
	}
	
	@Override
	public ILocalizer<Path, String> convert(String value) {
		Map<String, ILocalizer<Path, String>> nameToLocalizerMap = LocalizerConverter.getNameToLocalizerMap();
		if (!nameToLocalizerMap.containsKey(value.toLowerCase())) {
			throw new ParameterException(String.format("Localizer {} is not available.", value));
		}
		return LocalizerConverter.getNameToLocalizerMap().get(value.toLowerCase());
	}

}
