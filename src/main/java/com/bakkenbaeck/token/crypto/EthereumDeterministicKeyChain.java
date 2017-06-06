/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.crypto.hdshim;


import com.google.common.collect.ImmutableList;

import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;

import java.util.ArrayList;
import java.util.List;

/* package */ class EthereumDeterministicKeyChain extends DeterministicKeyChain {

    // IDENTITY_PATH = m/0'/1/0
    private static final ImmutableList<ChildNumber> IDENTITY_PATH =
            ImmutableList.of(
                    ChildNumber.ZERO_HARDENED,
                    ChildNumber.ONE,
                    ChildNumber.ZERO);

    // ETH44_ACCOUNT_ZERO_PATH = m/44'/60'/0'/0/0
    private static final ImmutableList<ChildNumber> ETH44_ACCOUNT_ZERO_PATH =
            ImmutableList.of(
                    new ChildNumber(44, true),
                    new ChildNumber(60, true),
                    ChildNumber.ZERO_HARDENED,
                    ChildNumber.ZERO,
                    ChildNumber.ZERO);

    /* package */ EthereumDeterministicKeyChain(final DeterministicSeed seed) {
        super(seed);
    }

    @Override
    public List<DeterministicKey> getKeys(KeyPurpose purpose, int numberOfKeys) {
        final List<DeterministicKey> keys = new ArrayList<>(1);

        final DeterministicKey key;
        switch (purpose) {
            case AUTHENTICATION:
                key = getKeyByPath(IDENTITY_PATH, true);
                break;
            case RECEIVE_FUNDS:
                key = getKeyByPath(ETH44_ACCOUNT_ZERO_PATH, true);
                break;
            case CHANGE:
            case REFUND:
            default:
                throw new RuntimeException("unsupported keypurpose");
        }

        keys.add(key);
        return keys;
    }
}
