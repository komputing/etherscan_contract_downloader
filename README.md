# What is it

Downloads verified contracts from etherscan. The main intention was to gather statistics about the usage of NatSpec and RadSpec.
But it is also used to gather more statistics via [komputing/contract_metrics](https://github.com/komputing/contract_metrics) and feed the [ethereum-lists/4byte directory](https://github.com/ethereum-lists/4bytes)

If you find other use-cases please let us know.


# How to use it?

Download the index file [from here](https://etherscan.io/exportData?type=open-source-contract-codes) (it has a CAPTCHA so this is still a manual step) and run the downloader (e.g. via `./gradlew run`)

# License 

MIT

And when using the downloader please obey the TOS of etherscan.
