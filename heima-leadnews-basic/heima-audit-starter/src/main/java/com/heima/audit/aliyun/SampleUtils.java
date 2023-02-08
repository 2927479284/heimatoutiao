package com.heima.audit.aliyun;

import com.aliyun.imageaudit20191230.models.ScanTextResponse;
import com.aliyun.imageaudit20191230.models.ScanTextResponseBody;
import com.aliyun.tea.TeaException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Getter
@Setter
public class SampleUtils {

    /**
     * 使用AK&SK初始化账号Client
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public com.aliyun.imageaudit20191230.Client createClient(String accessKeyId, String accessKeySecret) {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "imageaudit.cn-shanghai.aliyuncs.com";
        try {
            return new com.aliyun.imageaudit20191230.Client(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Map<String,String> checkText(String context) {
        // 工程代码泄露可能会导致AccessKey泄露，并威胁账号下所有资源的安全性。以下代码示例仅供参考，建议使用更安全的 STS 方式，更多鉴权访问方式请参见：https://help.aliyun.com/document_detail/378657.html
        com.aliyun.imageaudit20191230.Client client = new SampleUtils().createClient("LTAI5t8PZ6BLWhBRpkqGmKGu", "aOKth2RVfSN921TZKXakvHc7TvJq9V");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels labels0 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestLabels()
                .setLabel("abuse");
        com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestTasks tasks0 = new com.aliyun.imageaudit20191230.models.ScanTextRequest.ScanTextRequestTasks()
                .setContent(context);
        com.aliyun.imageaudit20191230.models.ScanTextRequest scanTextRequest = new com.aliyun.imageaudit20191230.models.ScanTextRequest()
                .setTasks(java.util.Arrays.asList(
                        tasks0
                ))
                .setLabels(java.util.Arrays.asList(
                        labels0
                ));
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        String suggestion = "";
        try {
            // 复制代码运行请自行打印 API 的返回值
            ScanTextResponse scanTextResponse = client.scanTextWithOptions(scanTextRequest, runtime);
            System.out.println(scanTextResponse);
            List<ScanTextResponseBody.ScanTextResponseBodyDataElements> elements = scanTextResponse.getBody().getData().getElements();
            for (ScanTextResponseBody.ScanTextResponseBodyDataElements element : elements) {
                List<ScanTextResponseBody.ScanTextResponseBodyDataElementsResults> results = element.getResults();
                for (ScanTextResponseBody.ScanTextResponseBodyDataElementsResults result : results) {
                    suggestion = result.getSuggestion();
//                    System.out.println("suggestion："+suggestion);
                }
            }

        } catch (TeaException error) {
            // 如有需要，请打印 error
            com.aliyun.teautil.Common.assertAsString(error.message);
//            error.printStackTrace();

        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 如有需要，请打印 error
            com.aliyun.teautil.Common.assertAsString(error.message);
            _error.printStackTrace();
        }
        Map<String,String> map = new HashMap<>();
        map.put("suggestion",suggestion);
        return map;
    }
}