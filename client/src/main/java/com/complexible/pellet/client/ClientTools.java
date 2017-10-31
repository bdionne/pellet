package com.complexible.pellet.client;

import java.io.IOException;

import com.google.common.base.Throwables;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Set of static tools to work with client API
 *
 * @author edgar
 */
public final class ClientTools {
	
	public static int NO_CALLS = 0;
	public static long TIME_IN_CALLS = 0;

	public static <O> O executeCall(final Call<O> theCall) {
		try {
				long now = System.currentTimeMillis();
				Response<O> aResp = theCall.execute();
				TIME_IN_CALLS += System.currentTimeMillis() - now;
				if (NO_CALLS++ % 5 == 0) {
				System.out.println("Executed calls: " + NO_CALLS);
				System.out.println("Total time to date on server: " + TIME_IN_CALLS);
				}

			if (!aResp.isSuccess()) {
				throw new RuntimeException(String.format("Request call failed: [%d] %s",
				                                         aResp.code(), aResp.message()));
			}

			return aResp.body();
		}
		catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}
}
