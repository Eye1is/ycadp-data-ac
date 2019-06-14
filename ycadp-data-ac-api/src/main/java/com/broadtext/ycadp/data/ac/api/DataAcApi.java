package com.broadtext.ycadp.data.ac.api;

import com.broadtext.ycadp.data.ac.api.hystrix.DataAcFallbackFactor;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author PC-Xuchenglong
 */
@FeignClient(name = "${dataacService}",fallbackFactory = DataAcFallbackFactor.class)
public interface DataAcApi {

}
