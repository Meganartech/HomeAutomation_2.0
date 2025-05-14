import React, { useState, useRef } from "react";
import axios from "axios";
import { Link, useNavigate, useLocation } from "react-router-dom";
import AuthBackground from "../components/AuthBackground";

export default function OtpVerify() {
    const [otpData, setOtpData] = useState(["", "", "", "", "", ""]);
    const navigate = useNavigate();
    const input = useRef([]);
    const location = useLocation();
    const { email } = location.state || {};

    const resetForm = () => {
        setOtpData(["", "", "", "", "", ""]);
    }

    const handleChange = (index, value) => {
        if (!/^\d?$/.test(value)) return;
        const newOtp = [...otpData];
        newOtp[index] = value;
        setOtpData(newOtp);
        if (value && index < 5) {
            input.current[index + 1].focus();
        }
    };

    const handleKeyDown = (index, e) => {
        if (e.key === "Backspace" && !otpData[index] && index > 0) {
            input.current[index - 1].focus();
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const otp = otpData.join("");

        try {
            const { data, status } = await axios.post("http://localhost:8081/user/otp/verify", { email, otp });
            if (status === 200) {
                alert(data.message);
                navigate('/reset/password', { state: { email } });
            }
        } catch (err) {
            if (err.data?.error) {
                alert(`${err.data.error}`);
                console.log(err.data.error);
            } else {
                alert('OTP verification failed due to server error.');
            }
        } finally {
            resetForm();
        }
    };

    return (
        <>
            <AuthBackground>

                <h3 className="mb-3">Verify OTP</h3>

                <p className="text-muted mb-3">Enter 6 digit code sent to you at your email.</p>

                {/* form */}
                <form onSubmit={handleSubmit}>
                    <div className="d-flex justify-content-around mb-3">
                        {otpData.map((digit, i) => (
                            <input
                                key={i}
                                ref={el => input.current[i] = el}
                                type="text"
                                maxLength="1"
                                value={digit}
                                onChange={(e) => handleChange(i, e.target.value)}
                                onKeyDown={(e) => handleKeyDown(i, e)}
                                className="form-control text-center"
                                style={{ width: "50px", fontSize: "24px", borderBottom: "", borderRadius: "5px", background: "transparent" }}
                                required
                            />
                        ))}
                    </div>

                    <div className="mb-3">
                        <button type="submit" className="btn btn-dark w-100">Verify OTP</button>
                    </div>

                    <p className="text-center text-muted mb-3">
                        Didn’t receive a verification code?
                    </p>

                    <div className="text-center mb-3">
                        <Link className="btn btn-link p-0 text-dark">Resend Code</Link> |
                        <Link className="btn btn-link p-0 text-dark ms-1" to={"/forgot/password"}>Change Email</Link>
                    </div>
                </form>

            </AuthBackground>
        </>
    );
};