package li.auna.patches.shared.misc.settings

import app.revanced.patcher.patch.resourcePatch
import li.auna.patches.all.misc.resources.addResource
import li.auna.patches.all.misc.resources.addResources
import li.auna.patches.all.misc.resources.addResourcesPatch
import li.auna.patches.shared.misc.settings.preference.BasePreference
import li.auna.patches.shared.misc.settings.preference.IntentPreference
import li.auna.util.ResourceGroup
import li.auna.util.copyResources
import li.auna.util.getNode
import li.auna.util.insertFirst
import org.w3c.dom.Node

/**
 * A resource patch that adds settings to a settings fragment.
 *
 * @param rootPreference A pair of an intent preference and the name of the fragment file to add it to.
 * If null, no preference will be added.
 * @param preferences A set of preferences to add to the ReVanced fragment.
 */
fun settingsPatch(
    rootPreference: Pair<IntentPreference, String>? = null,
    preferences: Set<BasePreference>,
) = resourcePatch {
    dependsOn(addResourcesPatch)

    execute {
        copyResources(
            "settings",
            ResourceGroup("xml", "revanced_prefs.xml"),
        )

        addResources("shared", "misc.settings.settingsResourcePatch")
    }

    finalize {
        fun Node.addPreference(preference: BasePreference, prepend: Boolean = false) {
            preference.serialize(ownerDocument) { resource ->
                // TODO: Currently, resources can only be added to "values", which may not be the correct place.
                //  It may be necessary to ask for the desired resourceValue in the future.
                addResource("values", resource)
            }.let { preferenceNode ->
                insertFirst(preferenceNode)
            }
        }

        // Add the root preference to an existing fragment if needed.
        rootPreference?.let { (intentPreference, fragment) ->
            document("res/xml/$fragment.xml").use { document ->
                document.getNode("PreferenceScreen").addPreference(intentPreference, true)
            }
        }

        // Add all preferences to the ReVanced fragment.
        document("res/xml/revanced_prefs.xml").use { document ->
            val revancedPreferenceScreenNode = document.getNode("PreferenceScreen")
            preferences.forEach { revancedPreferenceScreenNode.addPreference(it) }
        }
    }
}
