package org.jboss.arquillian.container.tomcat.remote_6;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.jboss.arquillian.container.tomcat.remote_6.DeploymentException;
import org.jboss.arquillian.container.tomcat.remote_6.DeploymentInfo;


/**
 *
 * @author Ondrej Zizka
 */
public interface TomcatDeployerMBean {
    

   /** The default ObjectName */
   ObjectName OBJECT_NAME = ObjectNameFactory.create("TomcatDeployer");


   /**
    * The <code>deploy</code> method deploys a package identified by a URL
    * @param url an <code>URL</code> value
    */
   void deploy(URL url);

   /**
    * The <code>deploy</code> method deploys a package represented by a DeploymentInfo object.
    * @param deployment a <code>DeploymentInfo</code> value
    * @exception DeploymentException if an error occurs
    */
   void deploy(DeploymentInfo deployment) throws DeploymentException;


   
   
   
    class ObjectNameFactory {
        private static ObjectName create(String name){
            try {
                return new ObjectName(name);
            } catch (MalformedObjectNameException ex) {
                Logger.getLogger(TomcatDeployerMBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex) {
                Logger.getLogger(TomcatDeployerMBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }
    

    
}// class





