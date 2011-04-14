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

import org.jboss.arquillian.spi.TestEnricher;
import org.jboss.arquillian.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.testenricher.cdi.CDIInjectionEnricher;
import org.jboss.arquillian.testenricher.resource.ResourceInjectionEnricher;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Package the test enrichers supported by the Jetty Embedded 6.1.x Container plugin.
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @author Dan Allen
 * @version $Revision: $
 */
public class TomcatRemoteDeploymentAppender implements AuxiliaryArchiveAppender
{
   public Archive<?> createAuxiliaryArchive()
   {
      JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-tomcat-testenrichers.jar")
            .addPackages(
                true,
                CDIInjectionEnricher.class.getPackage(),
                ResourceInjectionEnricher.class.getPackage()
                // CDI support - see in-container-context.xml
                //javax.enterprise.inject.spi.BeanManager.class.getPackage(),
                //org.jboss.weld.resources.ManagerObjectFactory.class.getPackage()
                // TODO: ArquillianServletRunner not found.
                //org.jboss.arquillian.protocol.servlet.v_3.
                //org.jboss.arquillian.protocol.servlet_3.ServletTestRunner
                //org.jboss.arquillian.protocol.servlet.ServletUtil.class.getPackage()
            )
            /*
            .addPackage(org.jboss.arquillian.protocol.servlet.runner.SecurityActions.class.getPackage())
            .addAsManifestResource(
                "org/jboss/arquillian/protocol/servlet/v_3/web-fragment.xml",
                "web-fragment.xml")
            */
            .addAsServiceProvider(
                TestEnricher.class,
                CDIInjectionEnricher.class,
                ResourceInjectionEnricher.class)
            ;
      return archive;
   }
}
