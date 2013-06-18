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

package com.cubeia.backoffice.wallet.client;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubeia.backoffice.accounting.api.NegativeBalanceException;
import com.cubeia.backoffice.wallet.api.dto.Account;
import com.cubeia.backoffice.wallet.api.dto.AccountBalanceResult;
import com.cubeia.backoffice.wallet.api.dto.AccountQueryResult;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.Currency;
import com.cubeia.backoffice.wallet.api.dto.CurrencyListResult;
import com.cubeia.backoffice.wallet.api.dto.EntriesQueryResult;
import com.cubeia.backoffice.wallet.api.dto.Transaction;
import com.cubeia.backoffice.wallet.api.dto.TransactionQueryResult;
import com.cubeia.backoffice.wallet.api.dto.exception.AccountNotFoundException;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListAccountsRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListEntriesRequest;
import com.cubeia.backoffice.wallet.api.dto.request.ListTransactionsRequest;
import com.cubeia.backoffice.wallet.api.dto.request.TransferRequest;

public class WalletServiceClientHTTP implements WalletServiceClient {

	private static final String DEFAULT_CHAR_ENCODING = "UTF-8";

    private static final String MIME_TYPE_JSON = "application/json";

    private static final int NUMBER_OF_RETRIES = 3;

    private static final String PING = "/ping";

	private static final String ACCOUNT = "/account/id/%s";
	private static final String ACCOUNT_BALANCE = ACCOUNT+"/balance";
	private static final String ACCOUNT_OPEN = ACCOUNT+"/open";
	private static final String ACCOUNT_CLOSE = ACCOUNT+"/close";

	private static final String WALLET = "/wallet";
	private static final String ACCOUNTS = WALLET+"/accounts";
	private static final String TRANSACTIONS = WALLET+"/transactions";
	private static final String ENTRIES = WALLET+"/entries";
	private static final String LOOKUP_ACCOUNT_ID = WALLET+"/lookup/%s/%s";
	private static final String LOOKUP_ACCOUNT_BALANCE = WALLET+"/balance/%s/%s";

	private static final String TRANSACTION = WALLET+"/transaction/id/%s";

	private static final String DO_TRANSACTION = WALLET+"/transaction";

	private static final String CURRENCY = "/currency/id/%s";
    private static final String CURRENCIES = "/currencies";

	private Logger log = LoggerFactory.getLogger(getClass());

	private String baseUrl;

	private ObjectMapper mapper;


	public WalletServiceClientHTTP() {
		this("http://walletservice:8080/wallet-service");
	}

	public WalletServiceClientHTTP(String baseUrl) {
		this.baseUrl = baseUrl;
		initJsonMapper();
	}

	@Override
	public void setBaseUrl(String baseUrl) {
		log.debug("setting wallet service base url to: {}", baseUrl);
		this.baseUrl = baseUrl;
	}

	@Override
	public String getBaseUrl() {
		return baseUrl;
	}

	@Override
	public String ping() {
		GetMethod method = createGetMethod(baseUrl + PING);
		try {
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			return  method.getResponseBodyAsString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public Account getAccount(Long userId, String currencyCode) {
		String resource = String.format(baseUrl+LOOKUP_ACCOUNT_ID, userId, currencyCode);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			// Read the response body.
			InputStream body = method.getResponseBodyAsStream();
			return parseJson(body, Account.class);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}
	
	@Override
	public AccountBalanceResult getAccountBalanceByUserAndCurrency(Long userId, String currencyCode) {
		String resource = String.format(baseUrl+LOOKUP_ACCOUNT_BALANCE, userId, currencyCode);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
			assertResponseCodeOK(method, statusCode);
			// Read the response body.
			InputStream body = method.getResponseBodyAsStream();
			return parseJson(body, AccountBalanceResult.class);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public Account getAccountById(Long accountId) {
		String resource = String.format(baseUrl+ACCOUNT, accountId);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			// Read the response body.
			InputStream body = method.getResponseBodyAsStream();
			return parseJson(body, Account.class);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public void updateAccount(Account account) {
		String resource = String.format(baseUrl+ACCOUNT, account.getId());
		PutMethod method = new PutMethod(resource);
		try {
			String data = serialize(account);
			method.setRequestEntity(new StringRequestEntity(data, MIME_TYPE_JSON, DEFAULT_CHAR_ENCODING));
			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public AccountBalanceResult getAccountBalance(Long accountId) throws AccountNotFoundException {
		String resource = String.format(baseUrl+ACCOUNT_BALANCE, accountId);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);

			if (statusCode == HttpStatus.SC_OK) {
				// Read the response body.
				InputStream body = method.getResponseBodyAsStream();
                if (body == null) {
                    return null;
                }
                return parseJson(body, AccountBalanceResult.class);
			} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
				throw new AccountNotFoundException("GetAccountBalance failed. Account["+accountId+"] was not found remotely (HTTP 404 for '"+resource+"'");

			} else {
				throw new HttpException("Method failed: " + method.getStatusLine());
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public void closeAccount(Long accountId) throws AccountNotFoundException {
		String resource = String.format(baseUrl+ACCOUNT_CLOSE, accountId);
		PutMethod method = new PutMethod(resource);
		try {
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public void openAccount(Long accountId) {
		String resource = String.format(baseUrl+ACCOUNT_OPEN, accountId);
		PutMethod method = new PutMethod(resource);
		try {
			int statusCode = getClient().executeMethod(method);
			assertResponseCodeOK(method, statusCode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public CreateAccountResult createAccount(CreateAccountRequest request) {
		log.info("Create account request:"+request);
		PostMethod method = null;
		try {
			method = new PostMethod(baseUrl+ACCOUNTS);
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, MIME_TYPE_JSON, DEFAULT_CHAR_ENCODING));

			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            if (statusCode == HttpStatus.SC_OK) {
				InputStream body = method.getResponseBodyAsStream();
				return parseJson(body, CreateAccountResult.class);

			} else {
				throw new RuntimeException("Failed to create account, RESPONSE CODE: "+statusCode);
			}

		} catch (Exception e) {
			log.error("Failed to create account", e);
			throw new RuntimeException(e);
		} finally {
			if (method != null) {
				method.releaseConnection();
			}
		}
	}

	@Override
	public void transfer(Long accountId, TransferRequest request) {
		String resource = String.format(baseUrl+ACCOUNT, accountId);
		PostMethod method = new PostMethod(resource);
		try {
			String data = serialize(request);
			if(log.isTraceEnabled()) {
				log.trace("Transfer JSON data: " + data);
			}
			method.setRequestEntity(new StringRequestEntity(data, MIME_TYPE_JSON, DEFAULT_CHAR_ENCODING));
			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}


	@Override
	public TransactionResult doTransaction(TransactionRequest transaction) {
		PostMethod method = new PostMethod(baseUrl+DO_TRANSACTION);

		log.info("Do Transaction, POST to: " + baseUrl+DO_TRANSACTION);
		try {
			String data = serialize(transaction);
			method.setRequestEntity(new StringRequestEntity(data, MIME_TYPE_JSON, DEFAULT_CHAR_ENCODING));

			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            if (statusCode == HttpStatus.SC_OK) {
				InputStream body = method.getResponseBodyAsStream();
				return parseJson(body, TransactionResult.class);

			} if (statusCode == HttpStatus.SC_FORBIDDEN) {
				throw new NegativeBalanceException(-1L);
				
			} else {
				throw new RuntimeException("Failed to do transaction, RESPONSE CODE: "+statusCode);
			}
		} catch (NegativeBalanceException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public Transaction getTransactionById(Long transactionId) {
		String resource = String.format(baseUrl+TRANSACTION, transactionId);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			// Read the response body.
			InputStream body = method.getResponseBodyAsStream();
			return parseJson(body, Transaction.class);

		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			method.releaseConnection();
		}
	}


	@Override
	public AccountQueryResult listAccounts(ListAccountsRequest request) {
		String uri = baseUrl + ACCOUNTS;
		PutMethod method = new PutMethod(uri);
		try {
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, MIME_TYPE_JSON, DEFAULT_CHAR_ENCODING));

			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            if (statusCode == HttpStatus.SC_OK) {
				InputStream body = method.getResponseBodyAsStream();
				return parseJson(body, AccountQueryResult.class);

			} else {
				throw new RuntimeException("Failed to list accounts, RESPONSE CODE: " + statusCode + " url: " + uri);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed listing accounts via url " + uri, e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public EntriesQueryResult listEntries(ListEntriesRequest request) {
		String uri = baseUrl + ENTRIES;
		PutMethod method = new PutMethod(uri);
		try {
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, MIME_TYPE_JSON, DEFAULT_CHAR_ENCODING));

			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            if (statusCode == HttpStatus.SC_OK) {
				InputStream body = method.getResponseBodyAsStream();
				return parseJson(body, EntriesQueryResult.class);

			} else {
				throw new RuntimeException("Failed to list transactions, RESPONSE CODE: "+statusCode);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed listing entries via url " + uri, e);
		} finally {
			method.releaseConnection();
		}
	}


	@Override
	public TransactionQueryResult listTransactions(ListTransactionsRequest request) {
		String uri = baseUrl + TRANSACTIONS;
		PutMethod method = new PutMethod(uri);
		try {
			String data = serialize(request);
			method.setRequestEntity(new StringRequestEntity(data, MIME_TYPE_JSON, DEFAULT_CHAR_ENCODING));

			// Execute the HTTP Call
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            if (statusCode == HttpStatus.SC_OK) {
				InputStream body = method.getResponseBodyAsStream();
				return parseJson(body, TransactionQueryResult.class);

			} else {
				throw new RuntimeException("Failed to list transactions, RESPONSE CODE: "+statusCode + " url: " + uri);
			}

		} catch (Exception e) {
			throw new RuntimeException("Failed listing transactions via url " + uri, e);
		} finally {
			method.releaseConnection();
		}
	}

    @Override
    public CurrencyListResult getSupportedCurrencies() {
		String uri = baseUrl + CURRENCIES;
		String resource = String.format(uri);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			// Read the response body.
			InputStream body = method.getResponseBodyAsStream();
			return parseJson(body, CurrencyListResult.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed getting supported currencies via url " + uri, e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public void addCurrency(Currency currency) {
		String uri = baseUrl + CURRENCIES;
		PostMethod method = createPostMethod(uri);

		try {
			String data = serialize(currency);
			method.setRequestEntity(new StringRequestEntity(data, MIME_TYPE_JSON, DEFAULT_CHAR_ENCODING));

			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);
		} catch (Exception e) {
			throw new RuntimeException("Failed adding currency via url " + uri, e);
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public Currency getCurrency(String currencyCode) {
		String resource = String.format(baseUrl + CURRENCY, currencyCode);
		GetMethod method = createGetMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return null;
            }
            assertResponseCodeOK(method, statusCode);
			// Read the response body.
			InputStream body = method.getResponseBodyAsStream();
			return parseJson(body, Currency.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed getting currency via resource " + resource, e);
		} finally {
			method.releaseConnection();
		}
	}

    @Override
    public void updateCurrency(Currency currency) {
        String resource = String.format(baseUrl+CURRENCIES, currency.getCode());
        PutMethod method = new PutMethod(resource);
        try {
            String data = serialize(currency);
            method.setRequestEntity(new StringRequestEntity(data, MIME_TYPE_JSON, DEFAULT_CHAR_ENCODING));

            // Execute the HTTP Call
            int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            method.releaseConnection();
        }
    }

	@Override
	public void removeCurrency(String currencyCode) {
		String resource = String.format(baseUrl + CURRENCY, currencyCode);
		DeleteMethod method = createDeleteMethod(resource);

		try {
			// Execute the method.
			int statusCode = getClient().executeMethod(method);
            if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return;
            }
            assertResponseCodeOK(method, statusCode);
		} catch (Exception e) {
			throw new RuntimeException("Failed removing currency via url " + resource, e);
		} finally {
			method.releaseConnection();
		}
	}

	private HttpClient getClient() {
		return new HttpClient();
	}

	private void assertResponseCodeOK(HttpMethodBase method, int statusCode) throws HttpException {
		if (statusCode != HttpStatus.SC_OK) {
			if(log.isTraceEnabled()) {
				try {
					log.debug("Method call ended in " + statusCode + "; response page: " + method.getResponseBodyAsString());
				} catch (IOException e) {
					log.error("failed to read body", e);
				}
			}
			throw new HttpException("Method failed: " + method.getStatusLine());
		}
	}

	private GetMethod createGetMethod(String resource) {
		GetMethod method = new GetMethod(resource);
		// Provide custom retry handler 
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(NUMBER_OF_RETRIES, false));
		return method;
	}

	private DeleteMethod createDeleteMethod(String resource) {
		DeleteMethod method = new DeleteMethod(resource);
		// Provide custom retry handler
		method.getParams().setParameter(
				HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(NUMBER_OF_RETRIES, false));
		return method;
	}

	private PostMethod createPostMethod(String resource) {
		PostMethod method = new PostMethod(resource);
		// Provide custom retry handler
		method.getParams().setParameter(
				HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler(NUMBER_OF_RETRIES, false));
		return method;
	}

	/**
	 * Configure Jackson to use JAXB annotations (only)
	 */
	private void initJsonMapper() {
		mapper = new ObjectMapper();
		AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
		mapper.getDeserializationConfig().setAnnotationIntrospector(introspector);
		mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
	}

	private <T> T parseJson(InputStream data, Class<T> clazz) {
		try {
			return mapper.readValue(data, clazz);
		} catch (Exception e) {
			throw new RuntimeException("error parsing json", e);
		}
	}

	private String serialize(Object value) {
		try {
			return mapper.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException("error serializing json", e);
		}
	}
}