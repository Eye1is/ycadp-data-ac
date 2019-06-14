package com.broadtext.ycadp.data.ac.api.hystrix;

import com.broadtext.ycadp.data.ac.api.DataAcApi;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author PC-Xuchenglong
 */
@Component
public class DataAcFallbackFactor implements FallbackFactory<DataAcApi>{

    @Override
    public DataAcApi create(Throwable cause) {
        cause.printStackTrace();

        return new DataAcApi() {

        };
    }
}
