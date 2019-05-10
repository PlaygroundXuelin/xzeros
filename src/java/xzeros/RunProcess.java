package xzeros;

import com.google.common.collect.ImmutableList;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RunProcess {
  private static class StreamThread extends Thread {
    public StreamThread(final InputStream is, final StringBuilder resultSb, final int maxStreamLen) {
      super(
          new Runnable() {
            @Override public void run() {
              try ( Reader r = new InputStreamReader(is) ) {
                int total = 0;
                char[] cbuf = new char[1024];
                while (true) {
                  if (maxStreamLen >= 0 && total >= maxStreamLen) {
                    break;
                  }
                  int cnt = r.read(cbuf);
                  if (cnt > 0) {
                    resultSb.append(String.valueOf(cbuf, 0, cnt));
                    total += cnt;
                  }
                  if (cnt <= 0 || maxStreamLen >= 0 &&  total >= maxStreamLen) {
                    break;
                  }
                }

              }
              catch (Exception ex) {

              }
            }
          }
      );
    }
  }
  public static List<Object> exec(List<String> cmd, int maxStreamLen, long timeoutMs) {
    try {
      ProcessBuilder pb = new ProcessBuilder(cmd);
      final Process p = pb.start();
      final StringBuilder outS = new StringBuilder();
      final StringBuilder errS = new StringBuilder();

      Thread outTh = new StreamThread(p.getInputStream(), outS, maxStreamLen);
      outTh.start();
      Thread errTh = new StreamThread(p.getErrorStream(), errS, maxStreamLen);
      errTh.start();
      if (timeoutMs <= 0) {
        p.waitFor();
        outTh.join();
        errTh.join();
      } else {
        p.waitFor(timeoutMs, TimeUnit.MICROSECONDS);
        outTh.join(timeoutMs);
        errTh.join(timeoutMs);
      }
      return ImmutableList.of(p.exitValue(), outS.toString(), errS.toString());
    }
    catch (Exception ex) {
      return ImmutableList.of(-1000, "", ex.getClass().getName() + ": " + ex.getMessage());
    }
  }
}
