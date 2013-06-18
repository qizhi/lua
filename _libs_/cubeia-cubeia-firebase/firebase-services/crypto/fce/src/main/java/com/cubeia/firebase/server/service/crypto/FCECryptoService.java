package com.cubeia.firebase.server.service.crypto;

import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.server.gateway.comm.crypto.CryptoFilter;

public class FCECryptoService implements SystemCryptoProvider, Service {

	@Override
	public Class<CryptoFilter> getMinaEncryptionFilter() {
		return null;
	}

	@Override
	public SystemKeyStore getSystemKeyStore() {
		return null;
	}

	@Override
	public void destroy() { }

	@Override
	public void init(ServiceContext con) throws SystemException { }

	@Override
	public void start() { }

	@Override
	public void stop() { }
	
	@Override
	public boolean isEncryptionMandatory() {
		return false;
	}
}
