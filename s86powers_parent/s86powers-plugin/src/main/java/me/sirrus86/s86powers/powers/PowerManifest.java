package me.sirrus86.s86powers.powers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.bukkit.Material;

import me.sirrus86.s86powers.tools.version.MCVersion;

/**
 * Annotation which contains unique authoring information about this power. The information provided here is used when players view this power in the plugin's power database.
 */
@Retention(value = RetentionPolicy.RUNTIME)
public @interface PowerManifest {

	/**
	 * The person or alias who coded this particular power.
	 */
	public String author();
	
	/**
	 * The person or alias who came up with the idea for this power.
	 * <p>
	 * This may also be the author.
	 */
	public String concept();
	
	/**
	 * A decription of what this power does, how to use it, etc.
	 * <p>
	 * Field values from the power class can be inserted into the description by surrounding the field name in brackets. For example, if you have the field {@code int test = 5}, then by having the description say "There are [test] dollars in my wallet", it will be read in-game as "There are 5 dollars in my wallet".
	 * <p>
	 * The following field types are currently supported:<ul>
	 * <li>{@code boolean} - Text between [field] and [/field] will only display if the boolean value is true. To have text appear if the field is false you must create a dedicated field.
	 * <li>{@code ItemStack} - Text [item] is displayed as a user-friendly name for the item. [act:item] will display "left-click" or "right-click" depending on which action is appropriate for the specified item.
	 * <li>{@code long} - Text [long] is displayed as a user-friendly representation of the amount of time this field is set to. For example, {@code long time = PowerTime.toMillis(1, 30, 0)} would display [time] as "1 minute 30 seconds".
	 * </ul>
	 * All other field types are displayed as {@link Object#toString()}.
	 */
	public String description();
	
	/**
	 * Whether this power is incomplete. Default configuration prevents the plugin from loading incomplete powers. This value is false unless set to true.
	 * @return false unless otherwise assigned
	 */
	public boolean incomplete() default false;
	
	/**
	 * Material used to depict this power's icon when the GUI is used.
	 */
	public Material icon();
	
	/**
	 * The user-friendly name of this power. If the true name of your power should contain non-alphanumeric symbols or spaces, this is where you'd print the name.
	 */
	public String name();
	
	/**
	 * The {@link PowerType} of this power. This determines whether the power is considered offensive, defensive, passive, or utility.
	 */
	public PowerType type();
	
	/**
	 * The minimum required server version for this power to function properly. If not set, version 1.13 or greater is required.
	 */
	public MCVersion version() default MCVersion.v1_13;
	
}
