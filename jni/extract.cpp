/*
	This program reads sharegen.txt which contains t+2 elements.
	Also member share should be given to this program in order to
	solve the equation for extracting the symmetric key. Only a 
	member can extract the key as it requires the member share.
*/

#include "define.h"
#include <pthread.h>
#define t 1000000
using namespace std;
mpz_t array[t+1][3];
mpz_t qMPZ;

// Lambda function calculate lambda ai in given array
double Lambda(mpz_t z, mpz_t ai, mpz_t MPZcount) {
	
	unsigned long int i = 0;
	mpz_t tmp, exponent, inv, lambda, nume, denom, nume_inv, temp;

	mpz_init(tmp);
	mpz_init_set_ui(nume, 1);
	mpz_init(inv);
	mpz_init_set_ui(temp, 1);
	mpz_init(nume_inv);
    mpz_init_set_si(lambda, 1);
	mpz_init_set_ui(denom,1);
	mpz_init_set_ui(qMPZ, Q);
	mpz_init_set(exponent, qMPZ);
	int count =  mpz_get_si(MPZcount);

	mpz_sub_ui(exponent, qMPZ, 2);



	for (i=0;i<=count;++i) {

		if(mpz_cmp(ai, array[i][0]) != 0) {

			mpz_sub(tmp, array[i][0], ai);
			mpz_mul(temp, denom, tmp);
			mpz_mod(denom, temp, qMPZ);

			mpz_mul(nume, nume, array[i][0]);
			mpz_mod(nume, nume, qMPZ);
		}
	}
        	
	mpz_powm(inv, denom, exponent, qMPZ);

	mpz_mul(lambda, nume, inv);
	mpz_mod(lambda, lambda, qMPZ);
	mpz_init_set(z, lambda);

	mpz_clear(lambda);
	mpz_clear(denom);
	mpz_clear(nume);
	mpz_clear(tmp);
	
}



std::string extract ( int count , std::string sharegen , int arg1 , int arg2 ) {
	//__android_log_write(ANDROID_LOG_INFO, TAG, sharegen.c_str());
	
	mpz_init_set_si(array[count][0], arg1);
	mpz_init_set_si(array[count][1], arg2);
	
	//clock_t t1,t2;
    //t1=clock();


	unsigned long int value;
	long int i, j;
	double d;


	
	mpz_t sgra, gr, fact, z, partial_share, partial_share_tmp, mem_expo, o, key, qMPZ, eqMPZ, res, one, exponent, exp, eqMPZm2, tmp1, inv1, temp1, MPZcount;

	mpz_init_set_ui(tmp1, 0);
	mpz_init_set_ui(temp1, 1);
	mpz_init(inv1);

	mpz_init(MPZcount);
	mpz_init(gr);				// Stores g^r from sharegen.txt
	mpz_init(sgra);				// Stores sg^ra from sharegen.txt
	mpz_init(key);				// Stores calculated symmetric key
    mpz_init_set_si(o, 1);
	mpz_init(res);				// Sotres retrieved lambda value 
	mpz_init_set_ui(mem_expo, 1);
    mpz_init_set_si(one, 1);
    mpz_init_set_si(partial_share, 1);
	mpz_init(partial_share_tmp);
	mpz_init(z);
	mpz_init_set_ui(eqMPZ, EQ);
	mpz_init_set_ui(qMPZ, Q);
	mpz_init(exponent);
	mpz_init(exp);
	mpz_init(eqMPZm2);

	mpz_sub_ui(eqMPZm2, eqMPZ, 2);
	mpz_sub_ui(exponent, qMPZ, 2);
	mpz_sub_ui(exp, qMPZ, 1);

	// Reading T vector from "sharegen.txt" file
	FILE *input= fopen(sharegen.c_str(), "r+");

	fseek(input, 0, SEEK_END);
	
	unsigned long input_bytes = ftell(input);
	unsigned long offset_bytes = 0;
	unsigned int * buffer_input = (unsigned int*)malloc(input_bytes);
	
	if(!buffer_input){
		fclose(input);
		return "-1";
	}

	rewind(input);
	
	fread(buffer_input, 1, input_bytes, input);

	fclose(input);
	
	mpz_set_ui(sgra,buffer_input[0]);

	mpz_set_ui(gr,buffer_input[1]);

	for (i=0;i<count;i++) {
	   mpz_init(array[i][0]);
	   mpz_set_ui(array[i][0],buffer_input[2+((i+1)*3-3)]);
	   
	   mpz_init(array[i][1]);
	   mpz_set_ui(array[i][1],buffer_input[2+((i+1)*3-2)]);
	   
	   mpz_init(array[i][2]);
	   mpz_set_ui(array[i][2],buffer_input[2+((i+1)*3-1)]);
	}

	free(buffer_input);
	buffer_input = NULL;
	//fclose(input);

	for (j=0;j<count;++j) {
		mpz_sub(tmp1, array[count][0], array[j][0]);
	    mpz_powm(inv1, tmp1, exponent, qMPZ);
	    mpz_mul(temp1, array[j][2], array[count][0]);
	    mpz_mul(res, temp1, inv1);
		mpz_mod(res, res, qMPZ);


		mpz_powm(z, array[j][1], res, eqMPZ);	
		///
		mpz_mul(partial_share_tmp, partial_share, z);
		mpz_mod(partial_share, partial_share_tmp, eqMPZ);
		///
	}
	
	mpz_init_set_ui(MPZcount, count);

	Lambda(res, array[count][0], MPZcount);
	mpz_mul(mem_expo, res, array[count][1]);
	mpz_mod(mem_expo, mem_expo, qMPZ);

	mpz_powm(z, gr, mem_expo, eqMPZ);
	mpz_mul(o, partial_share, z);
	mpz_mod(o, o, eqMPZ);	
	mpz_powm(o, o, eqMPZm2, eqMPZ);
	mpz_mul(key, sgra, o);
	mpz_mod(key, key, eqMPZ);
	//gmp_printf("(%Zd , %Zd) -------> %Zd\t", array[count][0], array[count][1], key);

	std::string buffer;
	buffer.resize ( 1024 );

	gmp_sprintf ( (char*)buffer.data() , "%Zd" , key );
	//__android_log_write(ANDROID_LOG_INFO, TAG, buffer.data() );
	//__android_log_write(ANDROID_LOG_INFO, TAG, sharegen.c_str());

	//t2=clock();
	//float dif = ((float)t2-(float)t1);
	//float seconds = dif / CLOCKS_PER_SEC;
	//sprintf ( (char*)buffer.data ( ) , "Took %f\n" , seconds ); 
	//__android_log_write(ANDROID_LOG_INFO, TAG, buffer.data() );
	//cout <<"it takes:  " << seconds << "  seconds!" << endl;
	

	mpz_clear(sgra);
	mpz_clear(res);
	mpz_clear(gr);
	mpz_clear(o);
	mpz_clear(key);		
	mpz_clear(z);
	mpz_clear(partial_share);
	mpz_clear(mem_expo);
	mpz_clear(qMPZ);

	return buffer;

}
