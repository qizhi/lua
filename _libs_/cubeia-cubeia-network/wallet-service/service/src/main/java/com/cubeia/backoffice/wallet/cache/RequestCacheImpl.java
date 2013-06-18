/**
 * Copyright (C) 2010 Cubeia Ltd <info@cubeia.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.cubeia.backoffice.wallet.cache;


public class RequestCacheImpl implements RequestCache {
	
//	private static Logger log = Logger.getLogger(RequestCacheImpl.class);
//	
//	private TransactionDAO transactionDAO;
//	
//	private AbstractProtocolFactory responseFactory = new ResponseProtocolFactory();
//	
//	/**
//	 * DI by Spring
//	 * 
//	 * @param transactionDAO
//	 */
//	public void setTransactionDAO(TransactionDAO transactionDAO) {
//		this.transactionDAO = transactionDAO;
//	}
//	
//	@Transactional
//	@Override
//	public void cacheResponse(Protocol request, Protocol response) {
//		if (request.getKey() != null) {
//			try {
//				ResponseCache tx = new ResponseCache();
//				tx.setTxId(request.getKey().toString());
//				ByteArrayOutputStream stream = new ByteArrayOutputStream();
//				responseFactory.write(response, stream);
//				String resp = stream.toString();
//				tx.setResponse(resp);
//				tx.setSignature(request.getSignature());
//				transactionDAO.persist(tx);
//			} catch (Exception e) {
//				log.error("Could not store response ["+response+"]. ", e);
//			}
//		}
//	}
//	
//	@Transactional
//	@Override
//	public String getCachedResponse(Protocol cmd) {
//		if (cmd.getKey() != null) {
//			ResponseCache cache = transactionDAO.getByTxId(cmd.getKey());
//			if (cache != null) {
//				if (cache.getSignature() == cmd.getSignature()) {
//					return cache.getResponse();
//				} else {
//					return createErrorResponse(cmd, "Signature mismatch for cached transactional request");
//				}
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * Generate an ErrorResponse with the given message.
//	 * TODO: Add error code?	 
//	 *  
//	 * @param cmd
//	 * @param msg
//	 * @return
//	 */
//	private String createErrorResponse(Protocol cmd, String msg) {
//		ErrorResponse error = new ErrorResponse();
//		error.setTxId(cmd.getKey());
//		error.setDescription(msg);
//		
//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		responseFactory.write(error, stream);
//		return stream.toString();
//	}
	
}
