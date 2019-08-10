package com.client.traveller.ui.util

import java.io.IOException

class ApiException(message: String): IOException(message)
class NoInternetAvailableException(message: String): IOException(message)