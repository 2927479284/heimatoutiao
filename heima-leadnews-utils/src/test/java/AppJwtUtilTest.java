import com.heima.utils.common.AppJwtUtil;
import io.jsonwebtoken.Claims;

public class AppJwtUtilTest {

    public static void main(String[] args) {
        // 生成token测试
        System.out.println(AppJwtUtil.getToken(1102L));

        // 校验token测试
        Claims claims =
                AppJwtUtil.getClaimsBody(
                        "eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAADWL0QrDIAwA_yXPFYxNtfZvjM2Yg4IQCxuj_770YW93HPeF12iwQRZkXHl2tVB1xN47jpEdJlyE68OHlGGCVgZsGOdIlJDCBHqy3frRIcfdVU2f0o5iVs7drPRuLO_-P9d8n80aog_XDxuqJCKDAAAA.sOAYHz3zd0NK_6rzxN4xgbcPMY94BqjMSDVX5XDxfsG5Kv_ISMkKNIG4sWZ2y_OEGPxpUb7MkRHDEUWimcZd9w");
        int result = AppJwtUtil.verifyToken(claims);
        System.out.println(result);

        // 获取token中id
        System.out.println(claims.get("id"));
    }
}
