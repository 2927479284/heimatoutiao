import org.springframework.util.DigestUtils;

// md5 破解网站   https://www.cmd5.com/
public class DigestUtilsTest {

    public static void main(String[] args) {
        //md5 加密
        System.out.println(DigestUtils.md5DigestAsHex("abc".getBytes()));
        System.out.println(DigestUtils.md5DigestAsHex("abc".getBytes()));

        //加盐处理



    }
}
