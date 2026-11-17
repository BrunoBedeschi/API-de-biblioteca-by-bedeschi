package br.com.biblioteca.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Lê o corpo uma vez (para calcular o hash) e permite que o controller o leia de novo. */
class CorpoEmCacheRequest extends HttpServletRequestWrapper {

    private final byte[] corpo;

    CorpoEmCacheRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.corpo = StreamUtils.copyToByteArray(request.getInputStream());
    }

    byte[] corpo() {
        return corpo;
    }

    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream in = new ByteArrayInputStream(corpo);
        return new ServletInputStream() {
            @Override public boolean isFinished() { return in.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener listener) { throw new UnsupportedOperationException(); }
            @Override public int read() { return in.read(); }
        };
    }

    @Override
    public BufferedReader getReader() {
        String enc = getCharacterEncoding();
        Charset charset = enc == null ? StandardCharsets.UTF_8 : Charset.forName(enc);
        return new BufferedReader(new InputStreamReader(getInputStream(), charset));
    }
}
