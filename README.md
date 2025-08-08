A chat application where an user can chat with another user, send them pictures , documents etc.
Google Sign In enable করার জন্য ফায়ারবেস কনসোল থেকে Sign in Method এ নতুন provider হিসেবে Google সিলেক্ট করতে হবে। SHA-1 fingerprint এড করতে হবে। এই fingerprint
পেতে হলে android studio এর ডান পাশের gradle (হাতির ছবি দেওয়া) এ ক্লিক করতে হবে। সেখানে একটা অপশন আছে Execute Gradle Task এ ক্লিক করতে হবে। তারপর একটা টার্মিনাল আসবে। সেখানে `gradle signingreport`
লিখে run করতে হবে। 
![](Image/Screenshot%20(13).png)
fingerprint add করার পর `google-services.json` file download করে আগেরটাতে প্রতিস্থাপন করতে হবে। Android -> app -> res -> values -> strings.xml এ একটা 
string যোগ করতে হবে। সেটা হলো `google_web_client_id` এবং এর value হবে `web client id`। এই `web client id` পাওয়া যায় গুগল কনসোল থেকে। এই `web client id` এর মাধ্যমে গুগল সাইন ইন করা হয়। 
`web_client_id` সেট করতে হবে। Firebase এ project add করার সময় Google Console এ Automatically একটা project create হয় (একই নামে)। সেটা সিলেক্ট করতে হবে।
[Google Console](https://console.cloud.google.com/apis/credentials) থেকে Web Client থেকে OAuth 2.0 Client IDs থেকে পাওয়া যায়। 
![](Image/Screenshot%20(14).png)

Git testing
