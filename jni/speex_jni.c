#include <jni.h>

#include <string.h>
#include <unistd.h>

#include <speex/speex.h>

static int codec_open = 0;

static int dec_frame_size;
static int enc_frame_size;

static SpeexBits ebits, dbits;
void *enc_state;
void *dec_state;

jint JNICALL Java_org_wjd_speex_Speex_open(JNIEnv *env, jobject obj,
		jint compression) {
	int tmp;

	if (codec_open++ != 0)
		return (jint) 0;

	speex_bits_init(&ebits);
	speex_bits_init(&dbits);

	enc_state = speex_encoder_init(&speex_nb_mode);
	dec_state = speex_decoder_init(&speex_nb_mode);
	tmp = compression;
	speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);
	speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);
	speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);

	return (jint) 0;
}

jint Java_org_wjd_speex_Speex_encode(JNIEnv *env, jobject obj, jshortArray lin,
		jbyteArray encoded) {

	jshort buffer[enc_frame_size];
	jbyte output_buffer[enc_frame_size];

	if (!codec_open)
		return 0;

	speex_bits_reset(&ebits);

	(*env)->GetShortArrayRegion(env, lin, 0, enc_frame_size, buffer);
	speex_encode_int(enc_state, buffer, &ebits);
	int len = speex_bits_write(&ebits, (char *) output_buffer, enc_frame_size);
	(*env)->SetByteArrayRegion(env, encoded, 0, len, output_buffer);

	return (jint) len;
}

jint JNICALL Java_org_wjd_speex_Speex_decode(JNIEnv *env, jobject obj,
		jbyteArray encoded, jshortArray lin, jint size) {

	jbyte buffer[dec_frame_size];
	jshort output_buffer[dec_frame_size];
	jsize encoded_length = size;

	if (!codec_open)
		return 0;

	(*env)->GetByteArrayRegion(env, encoded, 0, encoded_length, buffer);
	speex_bits_read_from(&dbits, (char *) buffer, encoded_length);
	speex_decode_int(dec_state, &dbits, output_buffer);
	(*env)->SetShortArrayRegion(env, lin, 0, dec_frame_size, output_buffer);

	return (jint) dec_frame_size;
}

jint JNICALL Java_org_wjd_speex_Speex_getFrameSize(JNIEnv *env, jobject obj) {

	if (!codec_open)
		return 0;
	return (jint) enc_frame_size;

}

void JNICALL Java_org_wjd_speex_Speex_close(JNIEnv *env, jobject obj) {

	if (--codec_open != 0)
		return;

	speex_bits_destroy(&ebits);
	speex_bits_destroy(&dbits);
	speex_decoder_destroy(dec_state);
	speex_encoder_destroy(enc_state);
}
