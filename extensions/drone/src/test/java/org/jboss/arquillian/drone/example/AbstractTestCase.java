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
package org.jboss.arquillian.drone.example;

import java.io.File;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.drone.example.webapp.Credentials;
import org.jboss.arquillian.drone.example.webapp.LoggedIn;
import org.jboss.arquillian.drone.example.webapp.Login;
import org.jboss.arquillian.drone.example.webapp.User;
import org.jboss.arquillian.drone.example.webapp.Users;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * This class shares deployment method for all available tests.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public abstract class AbstractTestCase
{

   /**
    * Creates a WAR of a Weld based application using ShrinkWrap
    * 
    * @return WebArchive to be tested
    */
   @Deployment(testable = false)
   public static WebArchive createDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "weld-login.war")
            .addClasses(Credentials.class, LoggedIn.class, Login.class, User.class, Users.class)            
            .addAsWebInfResource(new File("src/test/webapp/WEB-INF/beans.xml"))
            .addAsWebInfResource(new File("src/test/webapp/WEB-INF/faces-config.xml"))            
            .addAsWebInfResource(new File("src/test/resources/import.sql"))
            .addAsWebResource(new File("src/test/webapp/index.html"))
            .addAsWebResource(new File("src/test/webapp/home.xhtml"))
            .addAsWebResource(new File("src/test/webapp/template.xhtml"))
            .addAsWebResource(new File("src/test/webapp/users.xhtml"))
            .addAsResource(new File("src/test/resources/META-INF/persistence.xml"), ArchivePaths.create("META-INF/persistence.xml"))
            .setWebXML(new File("src/test/webapp/WEB-INF/web.xml"));

      //war.as(ZipExporter.class).exportTo(new File("weld-login.war"), true);
      
      return war;
   }

}
