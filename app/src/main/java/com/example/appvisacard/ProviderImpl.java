package com.example.appvisacard;

import android.nfc.tech.IsoDep;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;

import java.io.IOException;

public class ProviderImpl implements IProvider {

    private final IsoDep isoDep;

    public ProviderImpl(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    @Override
    public byte[] transceive(final byte[] command) throws CommunicationException {
        try {
            if (isoDep != null && isoDep.isConnected()) {
                return isoDep.transceive(command);
            } else {
                throw new CommunicationException("IsoDep is not connected.");
            }
        } catch (IOException e) {
            throw new CommunicationException(e.getMessage());
        }
    }

    @Override
    public byte[] getAt() {
        if (isoDep != null) {
            byte[] historical = isoDep.getHistoricalBytes();
            if (historical != null) {
                return historical;
            } else {
                // Nếu không có Historical Bytes thì trả về HiLayer Response
                return isoDep.getHiLayerResponse();
            }
        }
        return null;
    }
}
