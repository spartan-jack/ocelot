package com.vistatec.ocelot.di;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.vistatec.ocelot.DefaultPlatformSupport;
import com.vistatec.ocelot.OSXPlatformSupport;
import com.vistatec.ocelot.OcelotApp;
import com.vistatec.ocelot.PlatformSupport;
import com.vistatec.ocelot.config.Configs;
import com.vistatec.ocelot.config.DirectoryBasedConfigs;
import com.vistatec.ocelot.config.JsonConfigService;
import com.vistatec.ocelot.config.LqiJsonConfigService;
import com.vistatec.ocelot.config.LqiJsonConfigTransferService;
import com.vistatec.ocelot.config.OcelotJsonConfigService;
import com.vistatec.ocelot.config.OcelotJsonConfigTransferService;
import com.vistatec.ocelot.config.ProfileConfigService;
import com.vistatec.ocelot.config.ProfileConfigTransferService;
import com.vistatec.ocelot.config.TransferException;
import com.vistatec.ocelot.events.api.EventBusWrapper;
import com.vistatec.ocelot.events.api.OcelotEventQueue;
import com.vistatec.ocelot.findrep.FindAndReplaceController;
import com.vistatec.ocelot.its.stats.model.ITSDocStats;
import com.vistatec.ocelot.lqi.LQIGridController;
import com.vistatec.ocelot.lqi.constants.LQIConstants;
import com.vistatec.ocelot.plugins.PluginManager;
import com.vistatec.ocelot.profile.ProfileManager;
import com.vistatec.ocelot.rules.RuleConfiguration;
import com.vistatec.ocelot.rules.RulesParser;
import com.vistatec.ocelot.services.ITSDocStatsService;
import com.vistatec.ocelot.services.OkapiXliffService;
import com.vistatec.ocelot.services.ProvenanceService;
import com.vistatec.ocelot.services.SegmentService;
import com.vistatec.ocelot.services.SegmentServiceImpl;
import com.vistatec.ocelot.services.XliffService;
import com.vistatec.ocelot.tm.TmManager;
import com.vistatec.ocelot.tm.TmPenalizer;
import com.vistatec.ocelot.tm.TmService;
import com.vistatec.ocelot.tm.gui.TmGuiManager;
import com.vistatec.ocelot.tm.okapi.OkapiTmManager;
import com.vistatec.ocelot.tm.okapi.OkapiTmService;
import com.vistatec.ocelot.tm.okapi.OkapiTmxWriter;
import com.vistatec.ocelot.tm.penalty.SimpleTmPenalizer;

/**
 * Main Ocelot object dependency context module.
 */
public class OcelotModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(OcelotModule.class);
    
    private static final String CONF_FOLDER = "conf";

    @Override
    protected void configure() {
        OcelotEventQueue eventQueue = new EventBusWrapper(new EventBus());
        bind(OcelotEventQueue.class).toInstance(eventQueue);
        ITSDocStats docStats = new ITSDocStats();
        bind(ITSDocStats.class).toInstance(docStats);

        bind(OcelotApp.class).in(Scopes.SINGLETON);

        PlatformSupport platformSupport = getPlatformSupport();

        bind(PlatformSupport.class).toInstance(platformSupport);

        JsonConfigService jsonCfgService = null;
		LqiJsonConfigService lqiCfgService = null;
		ProfileConfigService profileCfgService = null;
        RuleConfiguration ruleConfig = null;
        PluginManager pluginManager = null;
        TmManager tmManager = null;
        TmService tmService = null;
        TmPenalizer penalizer = null;
        TmGuiManager tmGuiManager = null;
        LQIGridController lqiGridController = null;
        FindAndReplaceController frController = null;
        ProfileManager profileManager = null;
        try {
            File ocelotDir = new File(System.getProperty("user.home"), ".ocelot");
            ocelotDir.mkdirs();
            File configFolder = getConfigurationFolder(ocelotDir);
            profileCfgService = setupProfileCfgService(configFolder);
            profileManager = new ProfileManager(configFolder, profileCfgService, eventQueue);
            File profileFolder = new File(configFolder, profileCfgService.getProfileName());
            
            //TODO CHECK 
            Configs configs = new DirectoryBasedConfigs(ocelotDir);

            jsonCfgService = setupJsonConfigService(profileFolder);
			lqiCfgService = setupLQIConfigService(profileFolder);
            ruleConfig = new RulesParser().loadConfig(configs.getRulesReader());

            pluginManager = new PluginManager(jsonCfgService, new File(ocelotDir, "plugins"), eventQueue);
            pluginManager.discover();
            eventQueue.registerListener(pluginManager);

            SegmentService segmentService = new SegmentServiceImpl(eventQueue);
            bind(SegmentService.class).toInstance(segmentService);
            eventQueue.registerListener(segmentService);

            File tm = new File(profileFolder, "tm");
            tm.mkdirs();
            OkapiTmxWriter tmxWriter = new OkapiTmxWriter(segmentService);
            eventQueue.registerListener(tmxWriter);
            tmManager = new OkapiTmManager(tm, jsonCfgService, tmxWriter);
            
            bind(OkapiTmManager.class).toInstance((OkapiTmManager) tmManager);
            penalizer = new SimpleTmPenalizer(tmManager);
            tmService = new OkapiTmService((OkapiTmManager)tmManager, penalizer, jsonCfgService);
            tmGuiManager = new TmGuiManager(tmManager, tmService, eventQueue, jsonCfgService);
            
            lqiGridController = new LQIGridController(lqiCfgService, eventQueue,
                                                      platformSupport);
            eventQueue.registerListener(lqiGridController);
            bind(LQIGridController.class).toInstance(lqiGridController);
            
            frController = new FindAndReplaceController(eventQueue);
            eventQueue.registerListener(frController);
        } catch (IOException | JAXBException | TransferException ex) {
            LOG.error("Failed to initialize configuration", ex);
            System.exit(1);
        }

        bind(RuleConfiguration.class).toInstance(ruleConfig);
        bind(PluginManager.class).toInstance(pluginManager);
        bind(TmManager.class).toInstance(tmManager);
        bind(TmPenalizer.class).toInstance(penalizer);
        bind(TmService.class).toInstance(tmService);
        bind(TmGuiManager.class).toInstance(tmGuiManager);
        bind(FindAndReplaceController.class).toInstance(frController);
        bind(ProfileManager.class).toInstance(profileManager);

		bindServices(eventQueue, profileCfgService, jsonCfgService, lqiCfgService, docStats);
    }
    
    private ProfileConfigService setupProfileCfgService(File configFolder) throws TransferException {
    	File profFile = new File(configFolder, "profile.json");
    	return new ProfileConfigService(new ProfileConfigTransferService(profFile));
    }

	public static PlatformSupport getPlatformSupport() {
        String os = System.getProperty("os.name");
        if (os.startsWith("Mac")) {
            return new OSXPlatformSupport();
        }
        return new DefaultPlatformSupport();
    }


	private void bindServices(OcelotEventQueue eventQueue,
	         ProfileConfigService profileCfgService, JsonConfigService jsonCfgService, LqiJsonConfigService lqiCfgService,
            ITSDocStats docStats) {
		
		bind(ProfileConfigService.class).toInstance(profileCfgService);
        bind(JsonConfigService.class).toInstance(jsonCfgService);
		bind(LqiJsonConfigService.class).toInstance(lqiCfgService);

        ProvenanceService provService = new ProvenanceService(eventQueue, jsonCfgService);
        bind(ProvenanceService.class).toInstance(provService);
        eventQueue.registerListener(provService);

        ITSDocStatsService docStatsService = new ITSDocStatsService(docStats, eventQueue);
        bind(ITSDocStatsService.class).toInstance(docStatsService);
        eventQueue.registerListener(docStatsService);

        XliffService xliffService = new OkapiXliffService(jsonCfgService, eventQueue);
        bind(XliffService.class).toInstance(xliffService);
        eventQueue.registerListener(xliffService);

    }


    private OcelotJsonConfigService setupJsonConfigService(File confFolder) throws TransferException {
    	
		File confFile = new File(confFolder, "ocelot_cfg.json");
		return new OcelotJsonConfigService(new OcelotJsonConfigTransferService(
		        confFile));
    	
    }
    
    
	private LqiJsonConfigService setupLQIConfigService(File confFolder)
	        throws TransferException, JAXBException {

		File configFile = new File(confFolder, "lqi_cfg.json");
		LqiJsonConfigService service = new LqiJsonConfigService(new LqiJsonConfigTransferService(
				configFile));
		// If the config file doesn't exist, initialize a default configuration.
		if (!configFile.exists()) {
		    LOG.info("Writing default LQI Grid configuration to " + configFile);
		    service.saveLQIConfig(LQIConstants.getDefaultLQIGrid());
		}
		return service;
	}
	
	private File getConfigurationFolder(File ocelotDir ){
		
		File confFolder = new File(ocelotDir, CONF_FOLDER);
    	if(!confFolder.exists()){
    		confFolder.mkdirs();
    	}
    	return confFolder;
	}
	
}