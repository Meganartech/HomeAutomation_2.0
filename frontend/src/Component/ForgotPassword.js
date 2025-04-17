import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import LoggedOutNavbar from "../Component/LoggedOutNavbar";
import Swal from "sweetalert2";

export default function ForgotPassword() {
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.post("http://localhost:8081/user/forgot/password", { email });
            Swal.fire({
                icon: 'success',
                text: "OTP sent to your email",
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
            navigate("/verify/otp", { state: { email } });
        } catch (err) {
            const errorMessage = err.response?.data?.error || "Failed to send OTP";
            setError(errorMessage);
            Swal.fire({
                icon: 'error',
                title: 'OTP Failed',
                text: errorMessage,
                customClass: {
                    popup: 'my-swal-popup',
                    title: 'my-swal-title',
                    htmlContainer: 'my-swal-content',
                    confirmButton: 'my-swal-confirm-button',
                }
            });
        }
    };

    return (
        <>
            <LoggedOutNavbar />
            <div className="container mt-20">
                <div className="row justify-content-center">
                    <div className="col-md-6">
                        <div className="card shadow border-0">
                            <div className="card-body">
                                <h4 className="card-title text-center text-123458">Forgot Password</h4>
                                <form onSubmit={handleSubmit}>
                                    <div className="mb-3">
                                        <label className="form-label text-123458">Email address</label>
                                        <input type="email" className="form-control" value={email} onChange={(e) => setEmail(e.target.value)} required />
                                    </div>
                                    {error && <p className="text-danger text-center fw-bold">{error}</p>}
                                    <button type="submit" className="btn btn-123458 w-100">Send OTP</button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
}
