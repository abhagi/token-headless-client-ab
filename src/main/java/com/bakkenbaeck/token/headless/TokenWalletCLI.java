package com.bakkenbaeck.token.headless;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.sampullara.cli.Args;
import com.sampullara.cli.Argument;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class TokenWalletCLI {
    @Argument(alias = "i", description = "File to read seed from")
    private static String input;

    @Argument(alias = "o", description = "Filename to save seed to")
    private static String output;

    public static void main(String[] args) throws Exception {
        final List<String> parse;
        try {
            parse = Args.parse(TokenWalletCLI.class, args);
        } catch (IllegalArgumentException e) {
            Args.usage(TokenWalletCLI.class);
            System.exit(1);
            return;
        }

        HDWallet wallet;
        if (input != null) {
            String seed = new String(Files.readAllBytes(Paths.get(input)));
            wallet = new HDWallet().init(seed);
        } else {
            wallet = new HDWallet().init(null);
        }

        if (output != null) {
            try( PrintWriter out = new PrintWriter( output )  ){
                out.println(wallet.getMasterSeed());
            }
        } else {
            System.out.println("Seed: " + wallet.getMasterSeed());
            System.out.println("Address: " + wallet.getOwnerAddress());
        }

    }
}
