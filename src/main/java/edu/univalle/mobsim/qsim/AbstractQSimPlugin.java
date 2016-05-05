package edu.univalle.mobsim.qsim;

import com.google.inject.Module;

import edu.univalle.mobsim.framework.AgentSource;
import edu.univalle.mobsim.framework.listeners.MobsimListener;
import edu.univalle.mobsim.qsim.interfaces.ActivityHandler;
import edu.univalle.mobsim.qsim.interfaces.DepartureHandler;
import edu.univalle.mobsim.qsim.interfaces.MobsimEngine;

import org.matsim.core.config.Config;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractQSimPlugin {

	private Config config;

	public AbstractQSimPlugin(Config config) {
		this.config = config;
	}

	public final Config getConfig() {
		return config;
	}
	public Collection<? extends Module> modules() {
		return Collections.emptyList();
	}
	public Collection<Class<? extends MobsimEngine>> engines() {
		return Collections.emptyList();
	}
	public Collection<Class<? extends MobsimListener>> listeners() {
		return Collections.emptyList();
	}
	public Collection<Class<? extends AgentSource>> agentSources() {
		return Collections.emptyList();
	}
	public Collection<Class<? extends DepartureHandler>> departureHandlers() {
		return Collections.emptyList();
	}
	public Collection<Class<? extends ActivityHandler>> activityHandlers() {
		return Collections.emptyList();
	}

}
