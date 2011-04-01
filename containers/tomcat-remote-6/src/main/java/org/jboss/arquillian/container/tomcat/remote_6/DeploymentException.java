package org.jboss.arquillian.container.tomcat.remote_6;

/**
 *
 * @author Ondrej Zizka
 */
class DeploymentException extends Exception {

    public DeploymentException(Throwable cause) {
        super(cause);
    }

    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeploymentException(String message) {
        super(message);
    }

    public DeploymentException() {
    }
    
    
    
}// class

