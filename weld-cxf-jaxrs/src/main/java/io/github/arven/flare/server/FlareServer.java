/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.arven.flare.server;

/**
 *
 * @author brian.becker
 */
public interface FlareServer {
    public void init();
    public void start();
    public void join();
    
    public void setWebAppDir(String dir);
    public void setWebAppContext(String dir);
    public void setPackage(Package pkg);
    public void setConfiguration(Object obj);
}
