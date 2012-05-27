package net.sf.buildbox.releasator.ng.impl;

import org.apache.maven.scm.log.ScmLogger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Petr Kozelka
 */
public class ReleasatorScmLogger implements ScmLogger {
    Logger logger = Logger.getLogger("net.sf.releasator");

    public ReleasatorScmLogger() {
        logger.setLevel(Level.FINEST);
    }

    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.FINE);
    }

    public void debug(String content) {
        System.out.println("/DEBUG/ " + content);
        logger.fine(content);
    }

    public void debug(String content, Throwable error) {
        logger.log(Level.FINE, content, error);
    }

    public void debug(Throwable error) {
        logger.log(Level.FINE, error.getLocalizedMessage(), error);
    }

    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    public void info(String content) {
        System.out.println("/INFO/ " + content);
        logger.info(content);
    }

    public void info(String content, Throwable error) {
        logger.log(Level.INFO, content, error);
    }

    public void info(Throwable error) {
        logger.log(Level.INFO, error.getLocalizedMessage(), error);
    }

    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    public void warn(String content) {
        System.out.println("/WARN/ " + content);
        logger.warning(content);
    }

    public void warn(String content, Throwable error) {
        logger.log(Level.WARNING, content, error);
    }

    public void warn(Throwable error) {
        logger.log(Level.WARNING, error.getLocalizedMessage(), error);
    }

    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    public void error(String content) {
        System.out.println("/ERROR/ " + content);
        logger.severe(content);
    }

    public void error(String content, Throwable error) {
        logger.log(Level.SEVERE, content, error);
    }

    public void error(Throwable error) {
        logger.log(Level.SEVERE, error.getLocalizedMessage(), error);
    }
}
