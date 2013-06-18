package com.cubeia.poker.shutdown.impl;

public interface ShutdownServiceMBean {

    /**
     * Prepares a shutdown of the system
     *
     * @return true if successful, false otherwise
     */
    public boolean prepareShutdown();

    /**
     * Finishes the shutdown. Should be called after prepare shutdown is called and
     * after verifying (manually or otherwise) that the system is ready to go down.
     *
     * @return true if successful, false otherwise (for example if prepare has not been called)
     */
    public boolean finishShutdown();
}
