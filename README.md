![](https://jitpack.io/v/expresspay/expresspay-android-sdk.svg) | [View SDK Wiki](https://github.com/ExpresspaySa/expresspay-android-sdk/wiki) | [Report new issue](https://github.com/ExpresspaySa/expresspay-android-sdk/issues/new)

# Expresspay Android SDK

Expresspay is a white-label payment software provider. Thanks to our 15+ years of experience in the payment industry, we’ve developed a state-of-the-art white-label payment system that ensures smooth and uninterrupted payment flow for merchants across industries.

<p align="center">
  <a href="https://expresspay.sa">
      <img src="/media/header.jpg" alt="Expresspay" width="400px"/>
  </a>
</p>

Expresspay Android SDK was developed and designed with one purpose: to help the Android developers easily integrate the Expresspay API Payment Platform for a specific merchant. 

The main aspects of the Expresspay Android SDK:

- [Kotlin](https://developer.android.com/kotlin) is the main language
- [Retrofit](http://square.github.io/retrofit/) is the API machine 
- [KDoc](https://kotlinlang.org/docs/reference/kotlin-doc.html) code coverage
- API debug [logging](https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor)
- Minimum SDK 16+
- Sample Application

To properly set up the SDK, read [Wiki](https://github.com/ExpresspaySa/expresspay-android-sdk/wiki) first.
To get used to the SDK, download a [sample app](https://github.com/ExpresspaySa/expresspay-android-sdk/tree/master/sample).

## Setup

Add to the root build.gradle:

```groovy
allprojects {
    repositories {
        ...
        maven {
            url "https://jitpack.io"
            credentials { username "jp_cppv4mkessjq12i4kqihrnjqvo" }
        }
    }
 }
```

Add to the package build.gradle:

```groovy
dependencies {
    implementation 'com.github.ExpresspaySa:expresspay-android-sdk-code:1.0.5'
}
```

Latest version is: [![](https://jitpack.io/v/ExpresspaySa/expresspay-android-sdk-code.svg)](https://jitpack.io/#ExpresspaySa/expresspay-android-sdk-code)


## Quick Payment Implementation
[**Card Payment**](https://github.com/ExpresspaySa/expresspay-android-sdk/wiki/Express-Quick-Card-Payment)
Start the card payment with one click, easy and short line of codes. It will help the developer to easily implement the payment using card in thier application. click the [link](https://github.com/ExpresspaySa/expresspay-android-sdk/wiki/Express-Quick-Card-Payment) for easy steps to start payments.


## Sample

| Sale | Recurring Sale | Capture |
|-|-|-|
| ![](/media/sales.gif) | ![](/media/recurring-sale.gif) | ![](/media/capture.gif) |

| Creditvoid | Get Trans Status | Get Trans Details |
|-|-|-|
| ![](/media/creditvoid.gif) | ![](/media/get-trans-status.gif) | ![](/media/get-trans-details.gif) |

## Getting help

To report a specific issue or feature request, open a [new issue](https://github.com/ExpresspaySa/expresspay-android-sdk/issues/new).

Or write a direct letter to the [support@expresspay.sa](mailto:support@expresspay.sa).

## License

MIT License. See the [LICENSE](https://github.com/ExpresspaySa/expresspay-android-sdk/blob/master/LICENSE) file for more details.

## Contacts

![](/media/footer.jpg)

Website: https://expresspay.sa  
Phone: [+966 920033633](tel:+966920033633)  
Email: [support@expresspay.sa](mailto:support@expresspay.sa)  
Address: Expresspay, Olaya Street, Riyadh, Saudi Arabia 

© 2022 - 2023 Expresspay. All rights reserved.
