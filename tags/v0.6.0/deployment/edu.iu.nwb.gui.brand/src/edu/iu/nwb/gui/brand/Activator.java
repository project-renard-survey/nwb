package edu.iu.nwb.gui.brand;

//import org.eclipse.jface.resource.ImageDescriptor;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin implements IStartup{

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.iu.nwb.gui.brand";
	// The shared instance
	private static Activator plugin;	
	private BundleContext bContext;
	private boolean alreadyLogged;
	private static final String nwb_greeting=
		"Welcome to the Network Workbench (NWB) tool that supports "+
		"the preprocessing, modeling, analysis, and visualization of "+
		"small, medium, and large networks. \n"+
		"The Network Workbench project is supported in part by the NSF IIS-0513650 award. "+
		"The primary investigators are Katy B�rner, Albert-L�szl� Barab�si, Santiago Schnell, "+
		"Alessandro Vespignani, Stanley Wasserman, Eric A. Wernert, and Craig Stewart. "+
		"The NWB tool was developed by Weixia (Bonnie) Huang, Dr. Santo Fortunato, "+
		"Bruce Herr, Russell Duhon, Timothy Kelley, M Felix Terkhorn, Cesar A. Hidalgo R., "+
        "Ben Markines, Dr. Soma Sanyal, Ann McCranie, Ramya Sabbineni, Vivek S. Thakres., "+
        "Dr. Alessandro Vespignani, and Dr. Katy B�rner.\n"+
        "It builds on the Cyberinfrastructure Shell (http://cishell.org) developed at the "+
        "Information Visualization Laboratory and the Cyberinfrastructure for Network Science Center, "+
        "both at Indiana University.\n"+
        "For more information on the Network Workbench project, "+
        "see http://nwb.slis.indiana.edu and https://nwb.slis.indiana.edu/community/.";


	
/*		"Welcome to use Network Workbench Tool "+
		"- A large-scale network analysis, modeling and visualization "+
		"cyberinfrastructure for network scientists.\n"+
		"The NWB Tool is funded by an NSF IIS-0513650 award to Katy B�rner, "+
		"Albert-Laszlo Barabasi, Santiago Schnell, Alessandro Vespignani, "+
		"Stanley Wasserman, and Eric Wernert, see  http://nwb.slis.indiana.edu.\n"+
		"It is developed by Weixia (Bonnie) Huang, Dr. Santo Fortunato, Ben Markines, Bruce Herr, "+
		"Ramya Sabbineni, Vivek S Thakres, Dr. Soma Sanyal and Dr. Katy B�rner.\n"+
		"It builds on the Cyberinfrastructure Shell (http://cishell.org) developed "+
		"at the InfoVis Lab and the Cyberinfrastructure for Network Science Center at Indiana University.\n\n"+
        "Please acknowledge this effort by citing:\n"+
        "Bruce Herr, Weixia Huang, Shashikant Penumarthy, and Katy B�rner. (in press). "+
        "Designing Highly Flexible and Usable Cyberinfrastructures for Convergence. "+
        "William S. Bainbridge (Ed.) Progress in Convergence. Annals of the New York Academy of Sciences.\n"+
        "http://cishell.org/papers/06-cishell.pdf";
*/
	
	/**
	 * The constructor
	 */
	public Activator() {
		plugin = this;
		alreadyLogged = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.bContext = context;
        if (!alreadyLogged) {
            earlyStartup();
        }

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public void earlyStartup(){
		//		TODO: Get log and print initial log blurb
/*        if (bContext != null) {
    		ServiceReference ref = bContext.getServiceReference(LogService.class.getName());
            
            if (ref != null) {
                LogService logger = (LogService)bContext.getService(ref);
                logger.log(LogService.LOG_INFO, nwb_greeting);
            }            
        }
*/
		if (bContext != null) {
            String greeting = null;
            Properties props = new Properties();

            try {
                props.load(bContext.getBundle().getEntry("/plugin.properties").openStream());                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            greeting = props.getProperty("greeting", null);
            
            ServiceReference ref = bContext.getServiceReference(LogService.class.getName());
                
            if (ref != null && greeting != null) {
                alreadyLogged = true;
                
                LogService logger = (LogService)bContext.getService(ref);
                logger.log(LogService.LOG_INFO, greeting);
            }
        }
        
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
/*	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
*/	
}
