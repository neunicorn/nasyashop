import http from 'h6/http';
import {check, sleep} from 'k6';
import {Rate} from 'h6/metrics';

export const options = {
    vus: 5,
    duration: '1m',
};

const BASE_URL = 'http://localhost:3000';
const PAGE_SIZE = 20;

export default function(){
    const url = `${BASE_URL}/api/v1/products?page=0&size=${PAGE_SIZE}`;

    const params = {
        headers: {
            'Content-Type' : 'application/json',
            'Authorization': 'Bearer <GANTI MENGGUNAKAN TOKEN JWT>',
        }
    }

    const response = http.get(url, params);

    //check the response
    check(response, {
        'status is 200' : (r) => r.status === 200,
        'rate limit not exceeded' : (r) => r.status === 429,
    });

    console.log(`Status: ${response.status}, Response time: ${response.timings.duration} ms`);

    //short pause between request
    sleep(0.1);
}

