export const isAndroidApp = () => {
return (
typeof window !== 'undefined' &&
(
window.location.protocol === 'capacitor:' ||
navigator.userAgent.includes('wv')
)
);
};

export const isVoiceSupported = () => {
// Disable voice playback in Android app
if (isAndroidApp()) {
return false;
}

return (
typeof window !== 'undefined' &&
'speechSynthesis' in window &&
'SpeechSynthesisUtterance' in window
);
};

export const stopSpeech = () => {
if (
typeof window !== 'undefined' &&
'speechSynthesis' in window
) {
window.speechSynthesis.cancel();
}
};
