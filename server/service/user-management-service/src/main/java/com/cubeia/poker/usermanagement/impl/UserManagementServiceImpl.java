package com.cubeia.poker.usermanagement.impl;

import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.RoutableService;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.cubeia.poker.usermanagement.api.UserManagementService;
import org.apache.log4j.Logger;

import java.util.Timer;
import java.util.TimerTask;

public class UserManagementServiceImpl implements UserManagementService, Service, RoutableService {

    private static final Logger log = Logger.getLogger(UserManagementServiceImpl.class);
    private ServiceRouter router;

    Timer blockTimer = null;
    private static final int CheckForBlockInterval = 1000 * 2;
    private static final int CheckForBlockInitialDelay = 1000 * 2 * 60;

    @Override
    public void setRouter(ServiceRouter router) {
        this.router = router;
    }

    @Override
    public void onAction(ServiceAction e) {
    }

    @Override
    public void init(ServiceContext context) throws SystemException {
        log.debug("userManagementService STARTED! ");
    }

    @Override
    public void destroy() { }

    @Override
    public void start()
    {
        blockTimer = new Timer();
        blockTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Your database code here
                //notifyUserAboutBlock(1);
            }
        }, CheckForBlockInitialDelay, CheckForBlockInterval);
    }

    @Override
    public void stop()
    {
        if (blockTimer != null) {
            blockTimer.cancel();
        }
    }

    public void notifyUserAboutBlock(int userId) {
        String protocolValue = "Surpriiiise ! U have been blocked.";
        ServiceAction action = new ClientServiceAction(userId, -1, protocolValue.getBytes());
        router.dispatchToPlayer(userId, action);
    }
}