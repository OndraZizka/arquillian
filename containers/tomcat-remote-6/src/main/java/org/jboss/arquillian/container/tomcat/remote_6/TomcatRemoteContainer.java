/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.tomcat.remote_6;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.xpath.XPathExpressionException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * <p>Arquillian {@link DeployableContainer} implementation for an
 * Remote Tomcat server; responsible for both deployment operations.</p>
 *
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @version $Revision: $
 */
public class TomcatRemoteContainer implements DeployableContainer<TomcatRemoteConfiguration>
{
   private static final Logger log = Logger.getLogger(TomcatRemoteContainer.class.getName());

   
   private static final String TMPDIR_SYS_PROP = "java.io.tmpdir";
   
   private static final String URL_PATH_DEPLOY = "/deploy";
   
   private static final String URL_PATH_UNDEPLOY = "/undeploy";
   
   private static final String URL_PATH_LIST = "/list";
   
   

   /**
    * Tomcat container configuration
    */
   private TomcatRemoteConfiguration conf;
   
   private String adminBaseUrl;

   private String deploymentName;

   private boolean wasStarted;

   private final List<String> failedUndeployments = new ArrayList<String>();

   public Class<TomcatRemoteConfiguration> getConfigurationClass()
   {
      return TomcatRemoteConfiguration.class;
   }

   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 2.5");
   }
   
   @Override
   public void setup(TomcatRemoteConfiguration configuration)
   {
      this.conf = configuration;
      
      this.adminBaseUrl = String.format("http://%s:%s@%s:%d/manager",
              this.conf.getUser(), this.conf.getPass(), this.conf.getHost(), this.conf.getHttpPort() );
      
      // StringUtils.substringBefore(archive.getName(), ".")
   }

   @Override
   public void start() throws LifecycleException
   {
      // TODO: Check that Tomcat is running.
   }

   @Override
   public void stop() throws LifecycleException
   {
      // TODO: Shutdown on :8005?
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   @Override
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Not implemented");      
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   @Override
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Not implemented");      
   }

   
   /**
    * Deploys to remote Tomcat using it's /manager web-app's org.apache.catalina.manager.ManagerServlet.
    * @param archive
    * @return
    * @throws DeploymentException 
    */
    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        if (archive == null) {
            throw new IllegalArgumentException("archive must not be null");
        }

        final String archiveName = archive.getName();

        try {
            // Export to a file so we can send it over the wire
            final File archiveFile = new File(new File(System.getProperty("java.io.tmpdir")), archiveName);
            archive.as(ZipExporter.class).exportZip(archiveFile, true);

            // Split the suffix to get deployment.
            String name = archiveName.substring(0, archiveName.lastIndexOf("."));
            this.deploymentName = name;

            Builder builder = prepareClientWebResource(URL_PATH_DEPLOY)
                    // Context path.
                    .queryParam("path", "/"+name)
                    .accept(MediaType.TEXT_PLAIN_TYPE)
                    .type(MediaType.APPLICATION_OCTET_STREAM_TYPE);
            
            
            final String textResponse = builder.put(String.class, archiveFile);
                    

            try {
                if (!isCallSuccessful(textResponse)) {
                    throw new DeploymentException("Deployment failed, Tomcat says: "+textResponse);
                }
            } catch (Exception e) {
                throw new DeploymentException("Error parsing Tomcat's response.", e);
            }

            // Call has been successful, now we need another call to get the list of servlets
            final String subComponentsResponse = prepareClientWebResource(URL_PATH_LIST + name).get(String.class);

            return this.parseForProtocolMetaData(subComponentsResponse);
        } catch (XPathExpressionException e) {
            throw new DeploymentException("Error in creating / deploying archive", e);
        }
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException
    {
    }

  
    /**
     * Basic REST call preparation
     *
     * @return the resource builder to execute
     */
    private WebResource prepareClient() {
        return prepareClientWebResource("");
    }

    /**
     * Basic REST call preparation, with the additional resource url appended
     *
     * @param additionalResourceUrl url portion past the base to use
     * @return the resource builder to execute
     */
    private WebResource prepareClientWebResource(String additionalResourceUrl) {
            // HTTP Client
            final Client client = Client.create();
            // Auth
            client.addFilter( new HTTPBasicAuthFilter( this.conf.getUser(), this.conf.getPass()) );
            WebResource resource = client.resource( this.adminBaseUrl + URL_PATH_DEPLOY );
            return resource;
    }
    
    
    
    /**
     * Looks for a successful exit code given the response of the call
     *
     * @param textResponse XML response from the REST call
     * @return true if call was successful, false otherwise
     * @throws XPathExpressionException if the xpath query could not be executed
     */
    private boolean isCallSuccessful(String textResponse) {
        // OK - Deployed application at context path /debug
        // OK - Undeployed application at context path /debug
        return textResponse.contains("OK");
    }

    

   /**
     * Parses output of /manager/list
      OK - Listed applications for virtual host localhost
      /:running:0:ROOT
      /manager:running:1:manager
      /docs:running:0:docs
      /examples:running:0:examples
      /host-manager:running:0:host-manager
   */
    private ProtocolMetaData parseForProtocolMetaData(String textResponse) throws XPathExpressionException {
        final ProtocolMetaData protocolMetaData = new ProtocolMetaData();
        final HTTPContext httpContext = new HTTPContext(this.conf.getHost(), this.conf.getHttpPort());
        
        //BufferedInputStream bis = new BufferedInputStream( new ByteArrayInputStream( textResponse.getBytes()) );
        String[] lines = textResponse.split("\\n");

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] parts = line.split(":");
            if( parts.length < 4 ) continue;
            httpContext.add(new Servlet(parts[0], parts[3]));
        }

        protocolMetaData.addContext(httpContext);
        return protocolMetaData;
    }

    
   

    protected void startTomcatRemote() throws UnknownHostException, org.apache.catalina.LifecycleException
    {
    }

    protected void stopTomcatRemote() throws LifecycleException, org.apache.catalina.LifecycleException
    {
    }

}// class
