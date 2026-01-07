import http from 'k6/http';
import { check, sleep } from 'k6';

// Tokens generated for userId=1
const MINIMAL_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzY3ODA3NjI3LCJleHAiOjE3Njc4OTQwMjd9.-tELt3ytK56EQmLv7zFgx7Snbft2YuT_oKtyGeNX894";
const FULL_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsImVtYWlsIjoidXNlcjFAZXhhbXBsZS5jb20iLCJ1c2VybmFtZSI6InVzZXIxIiwic3ViIjoiMSIsImlhdCI6MTc2NzgwNzYyNywiZXhwIjoxNzY3ODk0MDI3fQ.3OMeruKxJ-JYNylYwd0O-k8US-bGEf_dG2zAnSJWqsE";

const BASE_URL = 'http://localhost:8080/api/v1/test';

export const options = {
    stages: [
        { duration: '30s', target: 50 }, // Ramp up to 50 users
        { duration: '1m', target: 50 },  // Stay at 50 users
        { duration: '10s', target: 0 },  // Ramp down
    ],
};

export default function () {
    // Uncomment the scenario you want to run:

    // 1. DB Test
    testDb();

    // 2. Redis Test
    //testRedis();

    // 3. Stateless Test
    //testStateless();

    // sleep(0.5); 
}

function testDb() {
    const params = { headers: { 'Authorization': `Bearer ${MINIMAL_TOKEN}` } };
    const res = http.get(`${BASE_URL}/db`, params);
    check(res, { 'status is 200': (r) => r.status === 200 });
}

function testRedis() {
    const params = { headers: { 'Authorization': `Bearer ${MINIMAL_TOKEN}` } };
    const res = http.get(`${BASE_URL}/redis`, params);
    check(res, { 'status is 200': (r) => r.status === 200 });
}

function testStateless() {
    const params = { headers: { 'Authorization': `Bearer ${FULL_TOKEN}` } };
    const res = http.get(`${BASE_URL}/stateless`, params);
    check(res, { 'status is 200': (r) => r.status === 200 });
}
