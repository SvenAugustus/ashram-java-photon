/*
 * Copyright 2018-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.flysium.io.photon.api.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.flysium.io.photon.api.ApiInvoker;
import com.github.flysium.io.photon.api.ApiRequest;
import com.github.flysium.io.photon.api.ApiResponse;
import com.github.flysium.io.photon.api.ExtendApiResponse;
import com.github.flysium.io.photon.api.dto.ApiConfiguration;
import com.github.flysium.io.photon.api.dto.ApiLogVO;
import com.github.flysium.io.photon.api.dto.AppSecret;
import com.github.flysium.io.photon.api.service.ApiQueryService;
import com.github.flysium.io.yew.common.logger.Logger;
import com.github.flysium.io.yew.common.logger.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.SortedSet;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * API调用代理类
 *
 * @author Sven Augustus
 */
public class ApiInvokeProxy<I extends ApiRequest, O extends ExtendApiResponse> extends
    BaseApiProxy {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApiInvokeProxy.class);

  private final ApiQueryService apiQueryService;

  public ApiInvokeProxy(ApiQueryService apiQueryService) {
    this.apiQueryService = apiQueryService;
  }

  /**
   * API调用，并返回JSON格式的响应报文
   *
   * @param apiId API编码
   * @param apiInvoker API业务实现类
   * @param request 请求对象
   * @param log 日志对象
   * @return JSON格式的响应报文
   */
  @SuppressWarnings("PMD.AvoidCatchingThrowable")
  public String invoke(String apiId, ApiInvoker<I, O> apiInvoker, I request, ApiLogVO log) {
    ApiInvokerContext<I> context;

    ApiResponse response = ApiResponse.success();
    try {
      JSONObject requestJson = (JSONObject) JSON.toJSON(request);
      if (StringUtils.isNotEmpty(log.getRequestMsg())) {
        log.setRequestMsg(requestJson.toJSONString());
      }
      context = getContext(apiId, request, log);
      boolean parameterCheck = parameterCheck(context);
      //  参数格式校验
      if (!parameterCheck) {
        return failAndLog(log, response, "参数格式校验错误");
      }
      //  签名校验
      Pair<Boolean, SortedSet<String>> signatureCheck = signatureCheck(context, requestJson);
      if (!signatureCheck.getLeft()) {
        String message = "签名失败, 参数: " + signatureCheck.getRight();
        LOGGER.error("API调用内部错误, logId = " + log.getLogId() + " : " + message);
        return failAndLog(log, response, message);
      }
    } catch (Throwable e) {
      LOGGER.error("API调用内部错误, logId = " + log.getLogId() + " : " + e.getMessage(), e);
      return failAndLog(log, response, "系统内部错误: " + e.getMessage());
    }
    response.setResultCode(ApiInvoker.SUCCESS);
    // 补充结果
    response(context, response);

    String responseString;
    ExtendApiResponse extendApiResponse = null;
    try {
      // 业务调用
      extendApiResponse = apiInvoker.invoke(context.getRequest());
    } catch (Throwable e) {
      LOGGER.error("API调用失败, logId = " + log.getLogId() + " : " + e.getMessage(), e);
      setException(response, e);
    } finally {
      logResult(log, response);
      response.setApiTransId(log.getLogId());
      //  合并结果，并签名
      responseString = result(context, response, extendApiResponse);
    }

    // 返回
    return responseString;
  }

  private ApiInvokerContext<I> getContext(String apiId, I request, ApiLogVO log) {
    // 获取密钥，并根据API编码、应用ID获取通知URL
    String appId = request.getAppId();
    AppSecret appSecret = super.checkAndGetAppSecret("API调用错误", apiQueryService, appId);
    ApiConfiguration apiConfiguration = super
        .checkAndGetApiConfiguration("API调用错误", apiQueryService, appId, apiId);
    log.setApiSpecId(apiConfiguration.getApiSpecId());
    log.setApiInstId(apiConfiguration.getApiInstId());
    // @formatter:off
    return ApiInvokerContext.<I>builder()
      .request(request)
      .signatureCheckEnabled(appSecret.isSignatureCheckEnabled())
      .apiKey(appSecret.getAppKey())
      .build();
    // @formatter:on
  }

  // 参数格式校验
  protected boolean parameterCheck(ApiInvokerContext<I> context) {
    I request = context.getRequest();
    // @formatter:off
    return !StringUtils.isEmpty(request.getAppId()) &&
      !StringUtils.isEmpty(request.getTenantId()) &&
      !StringUtils.isEmpty(request.getTransactionId()) &&
      !StringUtils.isEmpty(request.getTimestamp()) &&
      // 时间戳,年月日时分秒
      request.getTimestamp().length() == 14 &&
      !StringUtils.isEmpty(request.getNonceStr()) &&
      !StringUtils .isEmpty(request.getSign());
    // @formatter:on
  }

  // 签名校验
  protected Pair<Boolean, SortedSet<String>> signatureCheck(ApiInvokerContext<I> context,
      JSONObject requestJson) {
    if (context.isSignatureCheckEnabled()) {
      //  签名校验
      return super.signatureCheck(requestJson, context.getApiKey());
    } else {
      LOGGER.warn("警告：应用 appId= {} 没有做签名校验", context.getRequest().getAppId());
    }
    return Pair.of(true, null);
  }

  // 补充结果
  protected void response(ApiInvokerContext<I> context, ApiResponse response) {
    I request = context.getRequest();
    response.setAppId(request.getAppId());
    response.setTenantId(request.getTenantId());
    response
        .setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
    response.setNonceStr(RandomStringUtils.randomAlphanumeric(32));
  }

  // 合并结果，并签名
  protected String result(ApiInvokerContext<I> context, ApiResponse response,
      ExtendApiResponse extendApiResponse) {
    response.setSignType(context.getRequest().getSignType());
    // 合并结果
    JSONObject responseJson = (JSONObject) JSON.toJSON(response);
    if (extendApiResponse != null) {
      JSONObject extendResponseJson = (JSONObject) JSON.toJSON(extendApiResponse);
      extendResponseJson.forEach(responseJson::putIfAbsent);
    }
    // 对数据进行签名
    if (context.isSignatureCheckEnabled()) {
      super.signature(responseJson, context.getRequest().getSignType(), context.getApiKey());
    } else {
      responseJson.put(ApiRequest.PARAM_SIGN, context.getRequest().getSign());
    }

    return responseJson.toJSONString();
  }

  /**
   * 设置异常信息
   *
   * @param response 响应对象
   * @param e 异常
   */
  protected void setException(ApiResponse response, Throwable e) {
    response.setResultCode(ApiInvoker.FAIL);
    response.setErrCode(ApiInvoker.FAIL);
    response.setErrCodeDes(asException(e));
  }

  private String failAndLog(ApiLogVO log, ApiResponse response, String message) {
    response.setReturnCode(ApiInvoker.FAIL);
    response.setReturnMsg(message);
    logResult(log, response);
    return JSONObject.toJSONString(response);
  }

  private void logResult(ApiLogVO log, ApiResponse response) {
    if (ApiInvoker.SUCCESS.equals(response.getReturnCode()) || ApiInvoker.SUCCESS
        .equals(response.getResultCode())) {
      log.setResultCode(ApiInvoker.SUCCESS);
      log.setResultMsg(response.getReturnMsg());
    } else {
      log.setResultCode(StringUtils.defaultIfEmpty(response.getErrCode(), ApiInvoker.FAIL));
      log.setResultMsg(
          StringUtils.defaultIfEmpty(response.getErrCodeDes(), response.getReturnMsg()));
    }
  }

}
